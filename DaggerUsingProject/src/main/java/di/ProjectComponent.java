package di;

import dagger.Component;
import daggerize.UseGeneratedCode;

@Component(modules = { UseGeneratedCodeModule.class,
                       RandomInterfaceModule.class })
public interface ProjectComponent
{
  UseGeneratedCode build();
}
