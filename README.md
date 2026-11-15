# Medibook – Hospital Appointment Booking (DevOps 20-mark component)

Spring Boot (Java 17, Maven) app with in-memory storage.
Endpoints: `GET /api/doctors`, `GET /api/appointments`, `POST /api/appointments`, plus `GET /api/info` (ConfigMap values + pod name) and `GET /health`.

## Component 1 – CI (10)
**Git**: `git log --oneline --graph --all` (main + feature branches + merge commits).
**App**: `mvn clean package && java -jar target/medibook.jar`
```
curl -i localhost:8080/api/doctors
curl -i localhost:8080/api/appointments
curl -i -X POST localhost:8080/api/appointments -H 'Content-Type: application/json' -d '{"doctorId":1,"patientName":"Ravi","slot":"09:00"}'
# same booking again -> 409 Conflict
curl -i localhost:8080/api/info
curl -i localhost:8080/health
```
**Jenkins** (Ubuntu): `sudo apt install openjdk-17-jdk maven docker.io jenkins`; add `jenkins` to `docker` group, restart.
1. Manage Jenkins > Tools: Maven named `Maven3`, JDK named `JDK17`.
2. New Item > Pipeline > "Pipeline script from SCM" > your repo URL, script path `Jenkinsfile`.
3. Trigger: Poll SCM `* * * * *` (already in Jenkinsfile) or GitHub webhook `http://<jenkins>:8080/github-webhook/` (needs public URL, e.g. ngrok).
4. Demo: edit a doctor name in `BookingService`, commit, push, watch build start by itself; jar shows under "Last Successful Artifacts".
Enhancements covered: A (Test stage), B (Docker tag `BUILD_NUMBER-GITSHA`), C (`.github/workflows/ci.yml`).

## Component 2 – Config mgmt and K8s (10)
**Ansible**: role was created with `ansible-galaxy init roles/medibook`.
```
mvn clean package
cd ansible && ansible-playbook -i inventory playbook.yml     # expect failed=0
curl localhost:8080/health
```
Variable: `app_port` (override: `-e app_port=9090`). Handler: `Restart medibook`; re-running after a jar change triggers it.
**Docker**: `docker build -t medibook:1.0 .` then `docker run -d -p 8081:8080 medibook:1.0` and `docker images`.
**Kubernetes** (minikube shown):
```
minikube start
minikube image load medibook:1.0          # or: kind load docker-image medibook:1.0
kubectl apply -f k8s/
kubectl get deployments,services,pods -o wide
curl $(minikube ip):30080/api/info          # or: minikube service medibook-svc --url
kubectl scale deployment medibook --replicas=5 && kubectl get pods
kubectl scale deployment medibook --replicas=3
```
Rollback demo (Option A):
```
kubectl set image deployment/medibook medibook=medibook:broken   # pods ErrImagePull
kubectl rollout undo deployment/medibook
```
Enhancements covered: A rollback, B ConfigMap, C probes, D multi-stage Dockerfile, F `/health`.

## Not yet run
The sandbox this was authored in had no Maven Central access, so run `mvn clean package` once on your machine first.
