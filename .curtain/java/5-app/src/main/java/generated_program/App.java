package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.pulumi.pulumi.StackReference;
import com.pulumi.pulumi.pulumi.StackReferenceArgs;
import com.pulumi.kubernetes.core_v1.Secret;
import com.pulumi.kubernetes.core_v1.SecretArgs;
import com.pulumi.kubernetes.meta_v1.inputs.ObjectMetaArgs;
import com.pulumi.kubernetes.apps_v1.Deployment;
import com.pulumi.kubernetes.apps_v1.DeploymentArgs;
import com.pulumi.kubernetes.apps_v1.inputs.DeploymentSpecArgs;
import com.pulumi.kubernetes.meta_v1.inputs.LabelSelectorArgs;
import com.pulumi.kubernetes.core_v1.inputs.PodTemplateSpecArgs;
import com.pulumi.kubernetes.core_v1.inputs.PodSpecArgs;
import com.pulumi.kubernetes.core_v1.Service;
import com.pulumi.kubernetes.core_v1.ServiceArgs;
import com.pulumi.kubernetes.core_v1.inputs.ServiceSpecArgs;
import com.pulumi.kubernetes.networking.k8s.io.v1.Ingress;
import com.pulumi.kubernetes.networking.k8s.io.v1.IngressArgs;
import com.pulumi.kubernetes.networking.k8s.io.v1.inputs.IngressSpecArgs;
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
        var dbStack = new StackReference("dbStack", StackReferenceArgs.builder()
            .name("friel/4-db/dev")
            .build());

        var wordpressSecret = new Secret("wordpressSecret", SecretArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .namespace("awesome-site")
                .build())
            .stringData(Map.of("dbPassword", dbStack.outputs().applyValue(outputs -> outputs.dbPassword())))
            .build());

        var wordpress = new Deployment("wordpress", DeploymentArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .name("wordpress")
                .namespace("awesome-site")
                .labels(Map.of("app", "wordpress"))
                .build())
            .spec(DeploymentSpecArgs.builder()
                .selector(LabelSelectorArgs.builder()
                    .matchLabels(Map.of("app", "wordpress"))
                    .build())
                .replicas(3)
                .template(PodTemplateSpecArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                        .labels(Map.of("app", "wordpress"))
                        .build())
                    .spec(PodSpecArgs.builder()
                        .containers(ContainerArgs.builder()
                            .name("wordpress")
                            .image("wordpress:latest")
                            .ports(ContainerPortArgs.builder()
                                .containerPort(80)
                                .name("wordpress")
                                .build())
                            .env(
                                EnvVarArgs.builder()
                                    .name("WORDPRESS_DB_NAME")
                                    .value(dbStack.outputs().applyValue(outputs -> outputs.dbName()))
                                    .build(),
                                EnvVarArgs.builder()
                                    .name("WORDPRESS_DB_HOST")
                                    .value(dbStack.outputs().applyValue(outputs -> outputs.dbHost()))
                                    .build(),
                                EnvVarArgs.builder()
                                    .name("WORDPRESS_DB_USER")
                                    .value(dbStack.outputs().applyValue(outputs -> outputs.dbUser()))
                                    .build(),
                                EnvVarArgs.builder()
                                    .name("WORDPRESS_DB_PASSWORD")
                                    .valueFrom(EnvVarSourceArgs.builder()
                                        .secretKeyRef(SecretKeySelectorArgs.builder()
                                            .name(wordpressSecret.metadata().applyValue(metadata -> metadata.name()))
                                            .key("dbPassword")
                                            .build())
                                        .build())
                                    .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build());

        var service = new Service("service", ServiceArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .namespace("awesome-site")
                .name("wordpress")
                .labels(Map.of("app", "wordpress"))
                .build())
            .spec(ServiceSpecArgs.builder()
                .selector(Map.of("app", "wordpress"))
                .ports(ServicePortArgs.builder()
                    .port(80)
                    .targetPort(80)
                    .protocol("TCP")
                    .name("wordpress")
                    .build())
                .type("ClusterIP")
                .build())
            .build());

        var ingress = new Ingress("ingress", IngressArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .name("ingress")
                .namespace("awesome-site")
                .labels(Map.of("app", "wordpress"))
                .build())
            .spec(IngressSpecArgs.builder()
                .ingressClassName("nginx")
                .rules(IngressRuleArgs.builder()
                    .http(HTTPIngressRuleValueArgs.builder()
                        .paths(HTTPIngressPathArgs.builder()
                            .path("/")
                            .pathType("Prefix")
                            .backend(IngressBackendArgs.builder()
                                .service(IngressServiceBackendArgs.builder()
                                    .name("wordpress")
                                    .port(ServiceBackendPortArgs.builder()
                                        .number(80)
                                        .build())
                                    .build())
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build());

    }
}
