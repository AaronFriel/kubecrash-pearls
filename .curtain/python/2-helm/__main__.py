import pulumi
import pulumi_kubernetes as kubernetes

ingress_nginx = kubernetes.helm.v3.Release("ingress-nginx",
    namespace="admin-ingress-nginx",
    chart="ingress-nginx",
    repository_opts=kubernetes.helm.v3.RepositoryOptsArgs(
        repo="https://kubernetes.github.io/ingress-nginx",
    ),
    values={},
    create_namespace=True)
