import * as pulumi from "@pulumi/pulumi";
import * as kubernetes from "@pulumi/kubernetes";

const dbStack = new pulumi.StackReference("dbStack", {name: "friel/4-db/dev"});
const wordpressSecret = new kubernetes.core.v1.Secret("wordpress-secret", {
    metadata: {
        namespace: "awesome-site",
    },
    stringData: {
        dbPassword: dbStack.outputs.apply(outputs => outputs?.dbPassword),
    },
});
const wordpress = new kubernetes.apps.v1.Deployment("wordpress", {
    metadata: {
        name: "wordpress",
        namespace: "awesome-site",
        labels: {
            app: "wordpress",
        },
    },
    spec: {
        selector: {
            matchLabels: {
                app: "wordpress",
            },
        },
        replicas: 3,
        template: {
            metadata: {
                labels: {
                    app: "wordpress",
                },
            },
            spec: {
                containers: [{
                    name: "wordpress",
                    image: "wordpress:latest",
                    ports: [{
                        containerPort: 80,
                        name: "wordpress",
                    }],
                    env: [
                        {
                            name: "WORDPRESS_DB_NAME",
                            value: dbStack.outputs.apply(outputs => outputs?.dbName),
                        },
                        {
                            name: "WORDPRESS_DB_HOST",
                            value: dbStack.outputs.apply(outputs => outputs?.dbHost),
                        },
                        {
                            name: "WORDPRESS_DB_USER",
                            value: dbStack.outputs.apply(outputs => outputs?.dbUser),
                        },
                        {
                            name: "WORDPRESS_DB_PASSWORD",
                            valueFrom: {
                                secretKeyRef: {
                                    name: wordpressSecret.metadata.apply(metadata => metadata?.name),
                                    key: "dbPassword",
                                },
                            },
                        },
                    ],
                }],
            },
        },
    },
});
const service = new kubernetes.core.v1.Service("service", {
    metadata: {
        namespace: "awesome-site",
        name: "wordpress",
        labels: {
            app: "wordpress",
        },
    },
    spec: {
        selector: {
            app: "wordpress",
        },
        ports: [{
            port: 80,
            targetPort: 80,
            protocol: "TCP",
            name: "wordpress",
        }],
        type: "ClusterIP",
    },
});
const ingress = new kubernetes.networking.v1.Ingress("ingress", {
    metadata: {
        name: "ingress",
        namespace: "awesome-site",
        labels: {
            app: "wordpress",
        },
    },
    spec: {
        ingressClassName: "nginx",
        rules: [{
            http: {
                paths: [{
                    path: "/",
                    pathType: "Prefix",
                    backend: {
                        service: {
                            name: "wordpress",
                            port: {
                                number: 80,
                            },
                        },
                    },
                }],
            },
        }],
    },
});
