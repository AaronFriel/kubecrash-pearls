package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.civo.Firewall;
import com.pulumi.civo.FirewallArgs;
import com.pulumi.civo.KubernetesCluster;
import com.pulumi.civo.KubernetesClusterArgs;
import com.pulumi.civo.inputs.KubernetesClusterPoolsArgs;
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
        var firewall = new Firewall("firewall", FirewallArgs.builder()
            .region("NYC1")
            .createDefaultRules(true)
            .build());

        var cluster = new KubernetesCluster("cluster", KubernetesClusterArgs.builder()
            .region("NYC1")
            .firewallId(firewall.id())
            .pools(KubernetesClusterPoolsArgs.builder()
                .nodeCount(3)
                .size("g4s.kube.medium")
                .build())
            .build());

        ctx.export("clusterName", cluster.name());
    }
}
