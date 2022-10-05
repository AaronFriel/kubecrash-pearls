package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.kubernetes.helm.sh.v3.Release;
import com.pulumi.kubernetes.helm.sh.v3.ReleaseArgs;
import com.pulumi.kubernetes.helm.sh.v3.inputs.RepositoryOptsArgs;
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
        var ingressNginx = new Release("ingressNginx", ReleaseArgs.builder()
            .namespace("admin-ingress-nginx")
            .chart("ingress-nginx")
            .repositoryOpts(RepositoryOptsArgs.builder()
                .repo("https://kubernetes.github.io/ingress-nginx")
                .build())
            .values()
            .createNamespace(true)
            .build());

    }
}
