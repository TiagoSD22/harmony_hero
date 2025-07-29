# 🎸 Guitar Chord Visualizer

A full-stack web application to help guitarists visualize chord shapes across the fretboard using a comprehensive dictionary of 768+ unique chord shapes.

## 🏗️ Architecture

- **Frontend**: React 18+ with Vite for interactive chord exploration
- **Backend**: Scala 3 with Netty HTTP server serving REST APIs
- **Database**: Aurora PostgreSQL (via Localstack for local development)
- **Infrastructure**: Terraform for cloud-native provisioning
- **Orchestration**: Docker Compose for containerized development

## 📦 Project Structure

```
guitar-visualizer/
├── frontend/               # React app (Vite)
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── Dockerfile
├── backend/                # Scala 3 + Netty
│   ├── src/
│   ├── build.sbt
│   └── Dockerfile
├── infra/                  # Terraform + Localstack
│   ├── main.tf
│   ├── variables.tf
│   └── Dockerfile
├── shared/                 # Chord data JSON files
├── docker-compose.yml
├── Makefile
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 20+ (for frontend development)
- sbt (for backend development)
- Terraform (for infrastructure)

### Local Development

1. **Start all services:**
   ```bash
   make dev
   ```

2. **Individual service commands:**
   ```bash
   make infra-up     # Start Localstack and provision DB
   make backend-up   # Start Scala backend
   make frontend-up  # Start React frontend
   ```

3. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Localstack: http://localhost:4566

## 🎯 API Endpoints

- `GET /chords/{key}` - Get all chord shapes for a given key
- `GET /chords/{key}/{quality}` - Get all variations for a quality within a key
- `GET /chords/qualities` - Get all available chord qualities
- `GET /health` - Health check endpoint

## 🎼 Chord Data Structure

The application supports 12 musical keys (A, A#, B, C, C#, D, D#, E, F, F#, G, G#) with multiple chord qualities:
- Basic: major, minor, diminished, augmented
- Seventh: dominant7, major7, minor7, minor7b5
- Extended: dominant9, dominant11, dominant13, major9
- Suspended: sus2, sus4
- Added: add9, 6, minor6

## 🔧 Development

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```

### Backend (Scala 3 + Netty)
```bash
cd backend
sbt compile
sbt run
```

### Infrastructure (Terraform + Localstack)
```bash
cd infra
terraform init
terraform plan
terraform apply
```

## 🐳 Docker Commands

```bash
# Build all containers
docker-compose build

# Start in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## 📊 Database Schema

The PostgreSQL database includes:
- `keys` table: Musical keys (A, B, C, etc.)
- `qualities` table: Chord qualities (major, minor, etc.)
- `chords` table: Chord definitions linking keys and qualities
- `variations` table: Fingering variations for each chord

## 🧪 Testing

- Backend tests: `sbt test`
- Frontend tests: `npm test`
- API tests: Bruno collections in `api-tests/`

## 📝 License

MIT License - see LICENSE file for details.
