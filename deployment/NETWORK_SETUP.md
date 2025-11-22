# Docker Network Configuration for TestCraft Dashboard

## Network Architecture

The TestCraft Dashboard uses a custom Docker network with the following configuration:

### Network Details
- **Network Name**: `testcraft-network`
- **Subnet**: `172.20.0.0/16` (65,536 IP addresses)
- **Gateway**: `172.20.0.1`
- **Bridge Name**: `testcraft-br`

### Service IP Assignments
- **PostgreSQL Primary**: `172.20.0.10`
- **PostgreSQL Shadow**: `172.20.0.11`
- **Backend (Spring Boot)**: `172.20.0.20`
- **Frontend (Nginx)**: `172.20.0.30`
- **Nginx Reverse Proxy**: `172.20.0.40`

## Why This Network Design?

### 1. **Avoids Common Conflicts**
- Uses `172.20.x.x` subnet (rarely used by host networks)
- Avoids `172.17.x.x` (Docker default)
- Avoids `192.168.x.x` (common home/office networks)
- Avoids `10.x.x.x` (common corporate networks)

### 2. **Predictable IP Addresses**
- Fixed IP assignments prevent service discovery issues
- Makes debugging easier
- Consistent across restarts

### 3. **Isolated from Host**
- Services can't accidentally access host services
- Host can't accidentally access container services
- Clean separation of concerns

## Network Troubleshooting

### Check Network Status
```bash
# List all networks
docker network ls

# Inspect the testcraft network
docker network inspect testcraft-network

# Check network connectivity between services
docker-compose exec backend ping 172.20.0.10  # PostgreSQL
docker-compose exec backend ping 172.20.0.30  # Frontend
docker-compose exec nginx ping 172.20.0.20    # Backend
```

### Test Service Communication
```bash
# Test database connection from backend
docker-compose exec backend nc -zv 172.20.0.10 5432

# Test backend API from nginx
docker-compose exec nginx nc -zv 172.20.0.20 8090

# Test frontend from nginx
docker-compose exec nginx nc -zv 172.20.0.30 80
```

### Check DNS Resolution
```bash
# Test internal DNS
docker-compose exec backend nslookup postgres
docker-compose exec backend nslookup frontend
docker-compose exec backend nslookup nginx
```

### Network Performance Testing
```bash
# Test network latency
docker-compose exec backend ping -c 5 172.20.0.10

# Test bandwidth (if iperf3 is installed)
docker-compose exec backend iperf3 -c 172.20.0.30
```

## Common Network Issues & Solutions

### Issue: Services Can't Communicate
**Symptoms**: Connection refused, timeouts
**Solutions**:
```bash
# Check if services are on the same network
docker-compose exec backend ip route
docker-compose exec frontend ip route

# Verify network configuration
docker network inspect testcraft-network | grep -A 10 "Containers"
```

### Issue: IP Address Conflicts
**Symptoms**: Network creation fails, services can't start
**Solutions**:
```bash
# Check for conflicting networks
docker network ls | grep 172.20

# Remove conflicting network
docker network rm conflicting-network-name

# Recreate with different subnet
# Edit docker-compose.yml subnet: 172.21.0.0/16
```

### Issue: External Connectivity Problems
**Symptoms**: Can't clone Git repos, can't download packages
**Solutions**:
```bash
# Check DNS resolution
docker-compose exec backend nslookup github.com

# Check external connectivity
docker-compose exec backend ping -c 3 8.8.8.8

# Check proxy settings (if behind corporate firewall)
docker-compose exec backend env | grep -i proxy
```

## Network Security Considerations

### Firewall Rules
The network is isolated by default, but you can add additional security:

```bash
# Block external access to database (optional)
iptables -A DOCKER-USER -i testcraft-br -j DROP

# Allow only specific external access
iptables -A DOCKER-USER -i testcraft-br -p tcp --dport 80 -j ACCEPT
iptables -A DOCKER-USER -i testcraft-br -p tcp --dport 443 -j ACCEPT
```

### Network Monitoring
```bash
# Monitor network traffic
docker-compose exec nginx netstat -tuln
docker-compose exec backend netstat -tuln

# Check network usage
docker stats
```

## Custom Network Configuration

If you need to modify the network:

```yaml
# In docker-compose.yml
networks:
  testcraft-network:
    driver: bridge
    name: testcraft-network
    ipam:
      driver: default
      config:
        - subnet: 172.21.0.0/16  # Change subnet
          gateway: 172.21.0.1    # Change gateway
    driver_opts:
      com.docker.network.bridge.name: testcraft-br
      com.docker.network.bridge.enable_icc: "true"
      com.docker.network.bridge.enable_ip_masquerade: "true"
      com.docker.network.bridge.host_binding_ipv4: "0.0.0.0"
      com.docker.network.driver.mtu: "1500"
```

## Network Cleanup

```bash
# Stop and remove all containers
docker-compose down

# Remove the network
docker network rm testcraft-network

# Remove all volumes (optional)
docker-compose down -v

# Recreate everything
docker-compose up -d
```
