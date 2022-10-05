import pulumi
import pulumi_kubernetes as kubernetes

db_stack = pulumi.StackReference("dbStack", stack_name="friel/4-db/dev")
wordpress_secret = kubernetes.core.v1.Secret("wordpress-secret",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        namespace="awesome-site",
    ),
    string_data={
        "dbPassword": db_stack.outputs["dbPassword"],
    })
wordpress = kubernetes.apps.v1.Deployment("wordpress",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        name="wordpress",
        namespace="awesome-site",
        labels={
            "app": "wordpress",
        },
    ),
    spec=kubernetes.apps.v1.DeploymentSpecArgs(
        selector=kubernetes.meta.v1.LabelSelectorArgs(
            match_labels={
                "app": "wordpress",
            },
        ),
        replicas=3,
        template=kubernetes.core.v1.PodTemplateSpecArgs(
            metadata=kubernetes.meta.v1.ObjectMetaArgs(
                labels={
                    "app": "wordpress",
                },
            ),
            spec=kubernetes.core.v1.PodSpecArgs(
                containers=[kubernetes.core.v1.ContainerArgs(
                    name="wordpress",
                    image="wordpress:latest",
                    ports=[kubernetes.core.v1.ContainerPortArgs(
                        container_port=80,
                        name="wordpress",
                    )],
                    env=[
                        kubernetes.core.v1.EnvVarArgs(
                            name="WORDPRESS_DB_NAME",
                            value=%!v(PANIC=Format method: runtime error: invalid memory address or nil pointer dereference),
                        ),
                        kubernetes.core.v1.EnvVarArgs(
                            name="WORDPRESS_DB_HOST",
                            value=%!v(PANIC=Format method: runtime error: invalid memory address or nil pointer dereference),
                        ),
                        kubernetes.core.v1.EnvVarArgs(
                            name="WORDPRESS_DB_USER",
                            value=%!v(PANIC=Format method: runtime error: invalid memory address or nil pointer dereference),
                        ),
                        kubernetes.core.v1.EnvVarArgs(
                            name="WORDPRESS_DB_PASSWORD",
                            value_from=kubernetes.core.v1.EnvVarSourceArgs(
                                secret_key_ref=kubernetes.core.v1.SecretKeySelectorArgs(
                                    name=wordpress_secret.metadata.name,
                                    key="dbPassword",
                                ),
                            ),
                        ),
                    ],
                )],
            ),
        ),
    ))
service = kubernetes.core.v1.Service("service",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        namespace="awesome-site",
        name="wordpress",
        labels={
            "app": "wordpress",
        },
    ),
    spec=kubernetes.core.v1.ServiceSpecArgs(
        selector={
            "app": "wordpress",
        },
        ports=[kubernetes.core.v1.ServicePortArgs(
            port=80,
            target_port=80,
            protocol="TCP",
            name="wordpress",
        )],
        type="ClusterIP",
    ))
ingress = kubernetes.networking.v1.Ingress("ingress",
    metadata=kubernetes.meta.v1.ObjectMetaArgs(
        name="ingress",
        namespace="awesome-site",
        labels={
            "app": "wordpress",
        },
    ),
    spec=kubernetes.networking.v1.IngressSpecArgs(
        ingress_class_name="nginx",
        rules=[kubernetes.networking.v1.IngressRuleArgs(
            http=kubernetes.networking.v1.HTTPIngressRuleValueArgs(
                paths=[kubernetes.networking.v1.HTTPIngressPathArgs(
                    path="/",
                    path_type="Prefix",
                    backend=kubernetes.networking.v1.IngressBackendArgs(
                        service=kubernetes.networking.v1.IngressServiceBackendArgs(
                            name="wordpress",
                            port=kubernetes.networking.v1.ServiceBackendPortArgs(
                                number=80,
                            ),
                        ),
                    ),
                )],
            ),
        )],
    ))
