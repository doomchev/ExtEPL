package vm;

public class IfFalseGoto extends Command {
  public Command command;

  public IfFalseGoto() {
  }
  
  public IfFalseGoto(Command command) {
    this.command = command;
  }
  
  @Override
  public void setGoto(Command command) {
    this.command = command;
  }
  
  @Override
  public Command execute() {
    booleanStackPointer--;
    if(booleanStack[booleanStackPointer + 1]) return nextCommand;
    return command;
  }
  
  @Override
  public String toString() {
    return super.toString() + " " + command.number;
  }
}