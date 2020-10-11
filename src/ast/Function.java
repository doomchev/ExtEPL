package ast;

import static ast.Entity.addCommand;
import export.Chunk;
import java.util.LinkedList;
import java.util.ListIterator;
import static ast.Entity.i64Class;
import ast.Scope.ScopeEntry;
import vm.VMCall;
import vm.Command;
import vm.I64StackMoveReturnValue;
import vm.VMAllocate;
import vm.VMCallParam;
import vm.VMNewFunctionCall;
import vm.VMBase;
import vm.VMDeallocate;
import vm.VMReturnThis;
import vm.VMNewThis;

public class Function extends FlagEntity {
  public Code code = new Code();
  public Value formula;
  public Entity type = null;
  public final LinkedList<Variable> parameters = new LinkedList<>();
  public boolean isClassField = false, isNativeFunction = false;
  public Chunk form = null;
  public int varIndex = -1, paramIndex = -1;
  public Command startingCommand;
  
  public Function(ID name) {
    this.name = name;
    addFlags();
  }

  @Override
  public boolean isClassField() {
    return isClassField;
  }
  
  @Override
  public ID getID() {
    return functionID;
  }

  @Override
  public Chunk getForm() {
    return form;
  }
  
  @Override
  public LinkedList<? extends Entity> getChildren() {
    return parameters;
  }

  @Override
  public Entity getType() {
    return type;
  }

  @Override
  public Entity getChild(ID id) {
    if(id == typeID) return type;
    if(id == codeID) return code;
    if(id == formulaID) return formula;
    return null;
  }

  @Override
  public Function toFunction() {
    return this;
  }

  @Override
  public void addToScope(Scope parentScope) {
    if(!hasFlag(constructorID)) parentScope.add(this);
    code.addToScope(parentScope);
    currentFunction = this;
    for(Variable variable : parameters) code.scope.add(variable, variable.name);
  }

  @Override
  public void setTypes(Scope parentScope) {
    Function oldFunction = currentFunction;
    currentFunction = this;
    if(type.toClass() == null) type.setTypes(parentScope);
    type = type.toClass();
    for(Variable variable : parameters) {
      if(variable.type.toClass() == null) variable.type.setTypes(parentScope);
      variable.type = variable.type.toClass();
      Entity paramType = variable.type;
      paramIndex++;
      variable.index = paramIndex;
    }
    for(Variable variable : parameters) variable.setTypes(parentScope);
    code.setTypes(parentScope);
    currentFunction = oldFunction;
  }

  public void setParameterTypes(LinkedList<Entity> parameters
      , Scope parentScope) {
    for(Entity parameter : parameters) parameter.setTypes(parentScope);
  }

  @Override
  public Entity setCallTypes(LinkedList<Entity> parameters, Scope parentScope) {
    return type;
  }

  @Override
  public void move(Entity entity) {
    entity.moveToFunction(this);
  }

  @Override
  public void moveToClass(ClassEntity classEntity) {
    isClassField = true;
    classEntity.methods.add(this);
  }

  @Override
  public void moveToCode(Code code) {
    code.lines.add(this);
  }
  
  @Override
  public void functionToByteCode() {
    boolean isConstructor = hasFlag(ID.constructorID);
    VMBase.currentFunction = this;
    VMBase.currentCommand = null;
    if(isConstructor) addCommand(new VMNewThis(type.toClass()));
    if(varIndex >= 0) addCommand(new VMAllocate(varIndex + 1));
    code.toByteCode();
    if(code.scope != null)
      for(ScopeEntry entry : code.scope.entries)
        entry.entity.functionToByteCode();
    if(isConstructor) {
      int paramQuantity = VMBase.currentFunction.paramIndex
        + VMBase.currentFunction.varIndex + 2;
      if(paramQuantity > 0) addCommand(new VMDeallocate(paramQuantity));
      addCommand(new VMReturnThis());
    }
  }

  public void toByteCode(FunctionCall call) {
    if(!isNativeFunction) {
      addCommand(new VMNewFunctionCall());
      if(!call.parameters.isEmpty()) addCommand(new VMCallParam());
    }    
    ListIterator<Variable> iterator = parameters.listIterator();
    for(Entity parameter : call.parameters) {
      Entity type = parameter.getType();
      parameter.toByteCode();
      if(!iterator.hasNext()) throw new Error("Too many parameters in "
          + getName());
      conversion(type.toClass(), iterator.next().type.toClass());
    }
    if(iterator.hasNext()) throw new Error("Too few parameters in "
        + getName());
    functionToByteCode(call);
    if(!isNativeFunction) addCommand(new VMCall(this));
    conversion(type, call.convertTo);
  }
  
  @Override
  public void logScope(String indent) {
    if(code.scope != null) code.logScope(indent);
  }
}
 