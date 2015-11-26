package generated;

import javax.inject.Inject;

import daggerize.Base;
import other.RandomInterface;

public class CopiedFromOtherProcessor implements RandomInterface
{
  @Inject
  Base Base;

  @Inject
  public CopiedFromOtherProcessor() { }

  @Override
  public String toString()
  {
    return "daggerize.Base";
  }
}
