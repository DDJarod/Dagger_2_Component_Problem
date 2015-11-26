package di;

import dagger.MembersInjector;
import dagger.Module;
import dagger.Provides;
import daggerize.UseGeneratedCode;

@Module
public class UseGeneratedCodeModule
{
  @Provides
    UseGeneratedCode provideUseGeneratedCode(final MembersInjector<UseGeneratedCode> injector)
  {
    final UseGeneratedCode useGeneratedCode = new UseGeneratedCode();

    injector.injectMembers(useGeneratedCode);

    return useGeneratedCode;
  }
}
