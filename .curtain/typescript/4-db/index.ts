import * as pulumi from "@pulumi/pulumi";
import * as kubernetes from "@pulumi/kubernetes";
import * as random from "@pulumi/random";

const namespace = "awesome-site";
const rootPassword = new random.RandomPassword("rootPassword", {
    length: 24,
    special: false,
});
const userPassword = new random.RandomPassword("userPassword", {
    length: 24,
    special: false,
});
const secretResource = new kubernetes.core.v1.Secret("secret", {
    metadata: {
        namespace: namespace,
    },
    stringData: {
        rootPassword: rootPassword.result,
        userPassword: userPassword.result,
    },
});
const service = new kubernetes.core.v1.Service("service", {
    metadata: {
        name: "database-service",
        namespace: namespace,
    },
    spec: {
        ports: [{
            port: 3306,
            targetPort: 3306,
        }],
        selector: {
            app: "db",
        },
    },
});
const database = new kubernetes.apps.v1.StatefulSet("database", {
    metadata: {
        name: "database",
        namespace: namespace,
        labels: {
            app: "db",
        },
    },
    spec: {
        selector: {
            matchLabels: {
                app: "db",
            },
        },
        serviceName: "database-service",
        replicas: 1,
        template: {
            metadata: {
                labels: {
                    app: "db",
                },
            },
            spec: {
                containers: [{
                    name: "database",
                    image: "mariadb:10",
                    args: ["--default-authentication-plugin=mysql_native_password"],
                    env: [
                        {
                            name: "MYSQL_DATABASE",
                            value: "wordpress",
                        },
                        {
                            name: "MYSQL_USER",
                            value: "wordpress",
                        },
                        {
                            name: "MYSQL_PASSWORD",
                            valueFrom: {
                                secretKeyRef: {
                                    name: secretResource.metadata.apply(metadata => metadata?.name),
                                    key: "userPassword",
                                },
                            },
                        },
                        {
                            name: "MYSQL_ROOT_PASSWORD",
                            valueFrom: {
                                secretKeyRef: {
                                    name: secretResource.metadata.apply(metadata => metadata?.name),
                                    key: "rootPassword",
                                },
                            },
                        },
                    ],
                    ports: [{
                        containerPort: 3306,
                        name: "mysql",
                    }],
                    volumeMounts: [{
                        name: "database",
                        mountPath: "/var/lib/mysql",
                    }],
                }],
                volumes: [{
                    name: "database",
                    persistentVolumeClaim: {
                        claimName: "database",
                    },
                }],
            },
        },
        volumeClaimTemplates: [{
            metadata: {
                name: "database",
            },
            spec: {
                accessModes: ["ReadWriteOnce"],
                resources: {
                    requests: {
                        storage: "1Gi",
                    },
                },
            },
        }],
    },
});
export const dbHost = service.metadata.apply(metadata => metadata?.name);
export const dbUser = "wordpress";
export const dbName = "wordpress";
export const dbPassword = userPassword.result;
