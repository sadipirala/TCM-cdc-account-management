# CDCAM ConfigMap Configuration

---

## ConfigMap
Kubernetes [ConfigMaps](https://kubernetes.io/docs/concepts/configuration/configmap/)
can be used to externalize project properties and avoid the need
to modify code whenever one of these properties need to be updated.

### SpringBoot Configuration
To enable our SpringBoot application to read a target ConfigMap, a SpringCloud dependency
is needed to be included into the project:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-config</artifactId>
    <version>1.0.2.RELEASE</version>
</dependency>
```

Once the dependency is set, a `bootstrap.yml` file will be required under
your project's [resources](/src/main/resources) folder to detail the specifics  of which ConfigMap
should be loaded into the project.

```yaml
spring:
  cloud:
    kubernetes:
      reload:
        enabled: true
      config:
        enabled: true
        namespace: cdcam
        sources:
          - name: cdcam-config
```

#### Handling multiple environments on a single EKS Cluster
In the scenario you have two or more deployments using different Spring profiles
within the same EKS Cluster, you can have multiple `bootstrap.yml` files, each
for any of the supported profiles/environments. Spring will be able to load them
independently based on the postfix added to the file (i.e. `bootstrap-qa.yml`)

- bootstrap-qa1.yml
```yaml
spring:
  cloud:
    kubernetes:
      config:
        sources:
          - name: cdcam-config-qa1
```
- bootstrap-qa4.yml
```yaml
spring:
  cloud:
    kubernetes:
      config:
        sources:
          - name: cdcam-config-qa4
```

### Consuming the ConfigMap
Spring Cloud will automatically map the retrieved properties from the ConfigMap
as your typical property configure in any of your `application.properties` files as well
an `application.yml` file.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cdcam-config
data:
  app-config.yml: |-
    configs:
      verification:
        enabled: true
      serverUrl: https://demo.com
```

Taking as an example above snippet for a ConfigMap, you can create a configuration
class using `@ConfigurationProperties` and map the properties based on the keys used
in your ConfigMap.

#### Examples

```java
@Data
@Component
@ConfigurationProperties("configs")
public class ProjectConfigs {
    private VerificationProp verification;
    private String serverUrl;
}

class VerificationProp {
    private boolean enabled;
}
```

```java
@Component
public class ProjectConfigs {

    @Value("${configs.verification.enabled}")
    private boolean isVerificationEnabled;

    @Value("${configs.serverUrl}")
    private String serverUrl;
    
    public boolean isVerificationEnabled() {
        return isVerificationEnabled;
    }

    public boolean getServerUrl() {
        return serverUrl;
    }
}
```

## Setting up Pod read permissions
It is important to ensure your pod has the required read permission in order
to fetch the target EKS ConfigMap.

In order to achieve this, a `Role` and `RoleBinding` are required to be created
in which the `RoleBinding` should link with the `ServiceAccount` your Pod uses:

- Role
```yaml
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: my-application-role-view
  namespace: my-application-namespace
rules:
  - verbs:
      - get
      - list
      - watch
    apiGroups:
      - ''
    resources:
      - configmaps
      - secrets
```

- RoleBinding
```yaml
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: my-application-view
  namespace: my-application-namespace
subjects:
  - kind: ServiceAccount
    name: my-application-service-account
    namespace: my-application-namespace
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: my-application-role-view
```

## Setup for local development
As mentioned in a couple of sections above, since the properties from the ConfigMap
are loaded the same way as they would do from a `application.properties` or
`application.yml` file, you can configure in your local development Spring profile
properties file the same values and have your project interpret them the same way
they would do as if it was being read from the ConfigMap.

Additionally, it is required to create a `bootstrap-local.yml` file and set the following properties in order to disable
SpringCloud locally:
```yaml
spring:
  cloud:
    kubernetes:
      reload:
        enabled: false
      config:
        enabled: false
```
