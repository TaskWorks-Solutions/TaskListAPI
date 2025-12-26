# GitHub Secrets Configuration

## Required Secrets for Team Access

Add these secrets to your GitHub repository settings:

### Database Configuration
```
DB_URL=jdbc:postgresql://localhost:5432/tasklist
DB_USERNAME=tasklist_user
DB_PASSWORD=your_secure_password_here
```

### Container Registry
```
GHCR_TOKEN=github_pat_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## How to Add Secrets:

1. Go to your GitHub repository
2. Navigate to Settings → Secrets and variables → Actions
3. Click "New repository secret"
4. Add each secret from the list above

## Usage in Application

These secrets will be used in:
- application.properties (database connection)
- GitHub Actions workflows (Docker registry access)
- Docker Compose configuration

## Security Notes

- Never commit actual passwords to the repository
- Use strong, unique passwords for production
- Rotate secrets regularly
- Limit secret access to necessary team members only
