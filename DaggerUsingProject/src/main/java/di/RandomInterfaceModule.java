package di;

import dagger.Module;
import dagger.Provides;
import generated.*;
import other.RandomInterface;

@Module
public class RandomInterfaceModule
{
  @Provides
    RandomInterface provideRandomInterface(final GeneratedByOtherProcessor gen)
  {
    return gen;
  }
}
