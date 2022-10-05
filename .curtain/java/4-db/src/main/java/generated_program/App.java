package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.random.RandomPassword;
import com.pulumi.random.RandomPasswordArgs;
import com.pulumi.kubernetes.core.v1.Secret;
import com.pulumi.kubernetes.core.v1.SecretArgs;
import com.pulumi.kubernetes.meta.v1.inputs.ObjectMetaArgs;
import com.pulumi.kubernetes.core.v1.Service;
import com.pulumi.kubernetes.core.v1.ServiceArgs;
import com.pulumi.kubernetes.core.v1.inputs.ServiceSpecArgs;
import com.pulumi.kubernetes.apps.v1.StatefulSet;
import com.pulumi.kubernetes.apps.v1.StatefulSetArgs;
import com.pulumi.kubernetes.apps.v1.inputs.StatefulSetSpecArgs;
import com.pulumi.kubernetes.meta.v1.inputs.LabelSelectorArgs;
import com.pulumi.kubernetes.core.v1.inputs.PodTemplateSpecArgs;
import com.pulumi.kubernetes.core.v1.inputs.PodSpecArgs;
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
        final var namespace = "awesome-site";

        var rootPassword = new RandomPassword("rootPassword", RandomPasswordArgs.builder()
            .length(24)
            .special(false)
            .build());

        var userPassword = new RandomPassword("userPassword", RandomPasswordArgs.builder()
            .length(24)
            .special(false)
            .build());

        var secretResource = new Secret("secretResource", SecretArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .namespace(namespace)
                .build())
            .stringData(Map.ofEntries(
                Map.entry("rootPassword", rootPassword.result()),
                Map.entry("userPassword", userPassword.result())
            ))
            .build());

        var service = new Service("service", ServiceArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .name("database-service")
                .namespace(namespace)
                .build())
            .spec(ServiceSpecArgs.builder()
                .ports(ServicePortArgs.builder()
                    .port(3306)
                    .targetPort(3306)
                    .build())
                .selector(Map.of("app", "db"))
                .build())
            .build());

        var database = new StatefulSet("database", StatefulSetArgs.builder()
            .metadata(ObjectMetaArgs.builder()
                .name("database")
                .namespace(namespace)
                .labels(Map.of("app", "db"))
                .build())
            .spec(StatefulSetSpecArgs.builder()
                .selector(LabelSelectorArgs.builder()
                    .matchLabels(Map.of("app", "db"))
                    .build())
                .serviceName("database-service")
                .replicas(1)
                .template(PodTemplateSpecArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                        .labels(Map.of("app", "db"))
                        .build())
                    .spec(PodSpecArgs.builder()
                        .containers(ContainerArgs.builder()
                            .name("database")
                            .image("mariadb:10")
                            .args("--default-authentication-plugin=mysql_native_password")
                            .env(
                                EnvVarArgs.builder()
                                    .name("MYSQL_DATABASE")
                                    .value("wordpress")
                                    .build(),
                                EnvVarArgs.builder()
                                    .name("MYSQL_USER")
                                    .value("wordpress")
                                    .build(),
                                EnvVarArgs.builder()
                                    .name("MYSQL_PASSWORD")
                                    .valueFrom(EnvVarSourceArgs.builder()
                                        .secretKeyRef(SecretKeySelectorArgs.builder()
                                            .name(secretResource.metadata().applyValue(metadata -> metadata.name()))
                                            .key("userPassword")
                                            .build())
                                        .build())
                                    .build(),
                                EnvVarArgs.builder()
                                    .name("MYSQL_ROOT_PASSWORD")
                                    .valueFrom(EnvVarSourceArgs.builder()
                                        .secretKeyRef(SecretKeySelectorArgs.builder()
                                            .name(secretResource.metadata().applyValue(metadata -> metadata.name()))
                                            .key("rootPassword")
                                            .build())
                                        .build())
                                    .build())
                            .ports(ContainerPortArgs.builder()
                                .containerPort(3306)
                                .name("mysql")
                                .build())
                            .volumeMounts(VolumeMountArgs.builder()
                                .name("database")
                                .mountPath("/var/lib/mysql")
                                .build())
                            .build())
                        .volumes(VolumeArgs.builder()
                            .name("database")
                            .persistentVolumeClaim(PersistentVolumeClaimVolumeSourceArgs.builder()
                                .claimName("database")
                                .build())
                            .build())
                        .build())
                    .build())
                .volumeClaimTemplates(PersistentVolumeClaimArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                        .name("database")
                        .build())
                    .spec(PersistentVolumeClaimSpecArgs.builder()
                        .accessModes("ReadWriteOnce")
                        .resources(ResourceRequirementsArgs.builder()
                            .requests(Map.of("storage", "1Gi"))
                            .build())
                        .build())
                    .build())
                .build())
            .build());

        ctx.export("dbHost", service.metadata().applyValue(metadata -> metadata.name()));
        ctx.export("dbUser", "wordpress");
        ctx.export("dbName", "wordpress");
        ctx.export("dbPassword", userPassword.result());
    }
}
