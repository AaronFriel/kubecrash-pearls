import pulumi
import pulumi_kubernetes as kubernetes
import pulumi_random as random

namespace = "awesome-site"
root_password = random.RandomPassword("rootPassword",
    length=24,
    special=False)
user_password = random.RandomPassword("userPassword",
    length=24,
    special=False)
secret_resource = kubernetes.core.v1.Secret("secret",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        namespace=namespace,
    ),
    string_data={
        "rootPassword": root_password.result,
        "userPassword": user_password.result,
    })
service = kubernetes.core.v1.Service("service",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        name="database-service",
        namespace=namespace,
    ),
    spec=kubernetes.core.v1.ServiceSpecArgs(
        ports=[kubernetes.core.v1.ServicePortArgs(
            port=3306,
            target_port=3306,
        )],
        selector={
            "app": "db",
        },
    ))
database = kubernetes.apps.v1.StatefulSet("database",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        name="database",
        namespace=namespace,
        labels={
            "app": "db",
        },
    ),
    spec=kubernetes.apps.v1.StatefulSetSpecArgs(
        selector=kubernetes.meta.v1.LabelSelectorArgs(
            match_labels={
                "app": "db",
            },
        ),
        service_name="database-service",
        replicas=1,
        template=kubernetes.core.v1.PodTemplateSpecArgs(
            metadata=kubernetes.meta.v1.ObjectMetaArgs(
                labels={
                    "app": "db",
                },
            ),
            spec=kubernetes.core.v1.PodSpecArgs(
                containers=[kubernetes.core.v1.ContainerArgs(
                    name="database",
                    image="mariadb:10",
                    args=["--default-authentication-plugin=mysql_native_password"],
                    env=[
                        kubernetes.core.v1.EnvVarArgs(
                            name="MYSQL_DATABASE",
                            value="wordpress",
                        ),
                        kubernetes.core.v1.EnvVarArgs(
                            name="MYSQL_USER",
                            value="wordpress",
                        ),
                        kubernetes.core.v1.EnvVarArgs(
                            name="MYSQL_PASSWORD",
                            value_from=kubernetes.core.v1.EnvVarSourceArgs(
                                secret_key_ref=kubernetes.core.v1.SecretKeySelectorArgs(
                                    name=secret_resource.metadata.name,
                                    key="userPassword",
                                ),
                            ),
                        ),
                        kubernetes.core.v1.EnvVarArgs(
                            name="MYSQL_ROOT_PASSWORD",
                            value_from=kubernetes.core.v1.EnvVarSourceArgs(
                                secret_key_ref=kubernetes.core.v1.SecretKeySelectorArgs(
                                    name=secret_resource.metadata.name,
                                    key="rootPassword",
                                ),
                            ),
                        ),
                    ],
                    ports=[kubernetes.core.v1.ContainerPortArgs(
                        container_port=3306,
                        name="mysql",
                    )],
                    volume_mounts=[kubernetes.core.v1.VolumeMountArgs(
                        name="database",
                        mount_path="/var/lib/mysql",
                    )],
                )],
                volumes=[kubernetes.core.v1.VolumeArgs(
                    name="database",
                    persistent_volume_claim=kubernetes.core.v1.PersistentVolumeClaimVolumeSourceArgs(
                        claim_name="database",
                    ),
                )],
            ),
        ),
        volume_claim_templates=[kubernetes.core.v1.PersistentVolumeClaimArgs(
            metadata=kubernetes.meta.v1.ObjectMetaArgs(
                name="database",
            ),
            spec=kubernetes.core.v1.PersistentVolumeClaimSpecArgs(
                access_modes=["ReadWriteOnce"],
                resources=kubernetes.core.v1.ResourceRequirementsArgs(
                    requests={
                        "storage": "1Gi",
                    },
                ),
            ),
        )],
    ))
pulumi.export("dbHost", service.metadata.name)
pulumi.export("dbUser", "wordpress")
pulumi.export("dbName", "wordpress")
pulumi.export("dbPassword", user_password.result)
