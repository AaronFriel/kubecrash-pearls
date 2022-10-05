package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.kubernetes.Provider;
import com.pulumi.kubernetes.ProviderArgs;
import com.pulumi.kubernetes.core.v1.Namespace;
import com.pulumi.kubernetes.core.v1.NamespaceArgs;
import com.pulumi.kubernetes.meta.v1.inputs.ObjectMetaArgs;
import com.pulumi.resources.CustomResourceOptions;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        Pulumi.run(App::stack);
    }

    public static void stack(Context ctx) {
        final var config = ctx.config();
        final var kubernetesKubeconfig = config.get("kubernetesKubeconfig");
        var k8SProvider = new Provider("k8SProvider", ProviderArgs.builder()
            .kubeconfig(kubernetesKubeconfig)
            .build());

        var awesomeSite = new Namespace("awesomeSite", NamespaceArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .name("awesome-site")
                .build())
            .build(), CustomResourceOptions.builder()
                .provider(k8SProvider)
                .build());

        ctx.export("name", awesomeSite.metadata().applyValue(metadata -> metadata.name()));
    }
}
