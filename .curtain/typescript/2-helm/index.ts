import * as pulumi from "@pulumi/pulumi";
import * as kubernetes from "@pulumi/kubernetes";

const ingressNginx = new kubernetes.helm.v3.Release("ingress-nginx", {
    namespace: "admin-ingress-nginx",
    chart: "ingress-nginx",
    repositoryOpts: {
        repo: "https://kubernetes.github.io/ingress-nginx",
    },
    values: {},
    createNamespace: true,
});
