# Production Deployment Guide

## 🧹 Step 1: Clean Up Docker Environment

### Remove Dangling Images
```bash
# Find all dangling images
docker images --filter "dangling=true" -q

# Remove all dangling images
docker rmi $(docker images --filter "dangling=true" -q)
```

### Verify Clean State
```bash
docker images
```

Expected output should show only:
- `deployment-backend:latest`
- `deployment-frontend:latest`
- Base images (postgres, nginx, node, etc.)

## 📦 Step 2: Package Images for Production

### Option A: Package All Images (Recommended for Offline Deployment)
```bash
cd deployment
./package-images.sh
```

This will create:
- Individual `.tar` files for each image in `deployment/production-images/`
- A compressed archive `testcraft-images-YYYYMMDD_HHMMSS.tar.gz`
- A manifest file with instructions

### Option B: Push to Docker Registry (Recommended for Online Deployment)
```bash
# Tag images with registry
docker tag deployment-backend:latest your-registry.com/testcraft-backend:latest
docker tag deployment-frontend:latest your-registry.com/testcraft-frontend:latest

# Push to registry
docker push your-registry.com/testcraft-backend:latest
docker push your-registry.com/testcraft-frontend:latest
```

## 🚀 Step 3: Deploy to Production

### Transfer Files (If using packaged images)
```bash
# On local machine
scp deployment/testcraft-images-*.tar.gz user@production-server:/opt/testcraft/

# On production server
cd /opt/testcraft
tar -xzf testcraft-images-*.tar.gz
```

### Load Images (If using packaged images)
```bash
cd production-images/
docker load -i deployment-backend_latest_*.tar
docker load -i deployment-frontend_latest_*.tar
docker load -i postgres_16-alpine_*.tar
docker load -i nginx_alpine_*.tar
```

### Or Pull Images (If using registry)
```bash
docker pull your-registry.com/testcraft-backend:latest
docker pull your-registry.com/testcraft-frontend:latest
```

### Update docker-compose.yml (If needed)
If you're using pre-built images instead of building from Dockerfile, update `deployment/docker-compose.yml`:

```yaml
backend:
  image: deployment-backend:latest  # Instead of build:
  # ... rest of config

frontend:
  image: deployment-frontend:latest  # Instead of build:
  # ... rest of config
```

### Start Services
```bash
cd /opt/testcraft/deployment
docker-compose up -d
```

### Verify Deployment
```bash
# Check running containers
docker-compose ps

# Check logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Check health
curl http://localhost/api/actuator/health  # Backend health
curl http://localhost/health              # Nginx health
```

## 🔍 Step 4: Verify Production Setup

### Check Image Sizes
```bash
docker images | grep -E "(deployment-backend|deployment-frontend)"
```

### Check Container Status
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### Check Network
```bash
docker network inspect testcraft-network
```

### Check Volumes
```bash
docker volume ls | grep testcraft
```

## 📋 Pre-Deployment Checklist

- [ ] All dangling images removed
- [ ] Images tagged correctly
- [ ] Images packaged or pushed to registry
- [ ] Production environment variables configured
- [ ] Database migrations ready
- [ ] SSL certificates configured (if using HTTPS)
- [ ] Backup strategy in place
- [ ] Monitoring and logging configured

## 🔧 Common Commands

### View Logs
```bash
docker-compose logs -f [service-name]
```

### Restart Service
```bash
docker-compose restart [service-name]
```

### Stop All Services
```bash
docker-compose down
```

### Remove Everything (⚠️ Data Loss)
```bash
docker-compose down -v  # Removes volumes too
```

### Update Images
```bash
# Pull/build new images
docker-compose pull  # If using registry
docker-compose build  # If building locally

# Restart services with new images
docker-compose up -d
```

## 🆘 Troubleshooting

### Images Not Found
```bash
# Verify images exist
docker images | grep deployment

# Reload images if needed
docker load -i <image-file>.tar
```

### Port Conflicts
```bash
# Check what's using the ports
sudo lsof -i :80
sudo lsof -i :443
sudo lsof -i :8090
```

### Database Connection Issues
```bash
# Check postgres container
docker-compose logs postgres
docker-compose exec postgres psql -U postgres -d test_analytics
```

### Permission Issues
```bash
# Fix volume permissions
sudo chown -R 1000:1000 /path/to/volumes
```

## 📊 Production Monitoring

### Resource Usage
```bash
docker stats
```

### Disk Usage
```bash
docker system df
docker system df -v
```

### Container Inspection
```bash
docker inspect testcraft-backend
docker inspect testcraft-frontend
```
