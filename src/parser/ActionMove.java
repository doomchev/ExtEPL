package parser;

import ast.Entity;
import ast.EntityStack;

public class ActionMove extends Action {
  private final EntityStack<Entity> from, to;
  private final boolean copy;

  public ActionMove(EntityStack<Entity> from, EntityStack<Entity> to, boolean copy) {
    this.from = from;
    this.to = to;
    this.copy = copy;
  }
  
  @Override
  public void execute() throws base.ElException {
    currentAction = this;
    Entity entity = copy ? from.peek() : from.pop();
    if(log) log("MOVING " + from.name.string + " to " + to.name.string + "("
        + to.peek().toString() + ")");
    to.peek().move(entity);
    currentAction = nextAction;
  }

  @Override
  public String toString() {
    return from.name + " to " + to.name;
  }
}
