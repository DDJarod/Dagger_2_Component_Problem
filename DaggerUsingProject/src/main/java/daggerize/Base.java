package daggerize;

import javax.inject.Inject;

public class Base
{
  @Inject Base() { }

  @Override
  public String toString()
  {
    return "Base []";
  }
}
