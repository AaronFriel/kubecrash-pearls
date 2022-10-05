package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.kubernetes.Provider;
import com.pulumi.kubernetes.ProviderArgs;
import com.pulumi.kubernetes.apps.v1.DeploymentPatch;
import com.pulumi.kubernetes.apps.v1.DeploymentPatchArgs;
import com.pulumi.kubernetes.meta.v1.inputs.ObjectMetaPatchArgs;
import com.pulumi.kubernetes.apps.v1.inputs.DeploymentSpecPatchArgs;
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
        var k8SProvider = new Provider("k8SProvider", ProviderArgs.builder()
            .enableServerSideApply(true)
            .build());

        var deployment = new DeploymentPatch("deployment", DeploymentPatchArgs.builder()
            .metadata(ObjectMetaPatchArgs.builder()
                .namespace("awesome-site")
                .name("wordpress")
                .annotations(Map.of("pulumi.com/patchForce", "true"))
                .build())
            .spec(DeploymentSpecPatchArgs.builder()
                .replicas(3)
                .build())
            .build(), CustomResourceOptions.builder()
                .provider(k8SProvider)
                .build());

        ctx.export("status", deployment.status());
    }
}
