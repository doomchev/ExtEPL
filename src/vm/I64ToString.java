package vm;

public class I64ToString extends Command {
  @Override
  public void execute() {
    stringStack[stackPointer] = String.valueOf(i64Stack[stackPointer]);
    typeStack[stackPointer] = TYPE_STRING;
    currentCommand = nextCommand;
  }
}
