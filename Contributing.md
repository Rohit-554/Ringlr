# Contributing to Ringlr

Thank you for your interest in contributing to Ringlr! We welcome contributions from the community and are excited to welcome you aboard.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Process](#development-process)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)
- [Community](#community)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to project maintainers.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/Rohit-554/ringlr.git`
3. Create a new branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Test your changes thoroughly

## Development Process

1. Pick an issue to work on or create a new one
2. Comment on the issue to let others know you're working on it
3. Create a branch with a descriptive name (e.g., `feature/add-bluetooth-support`)
4. Write code and tests
5. Update documentation as needed
6. Submit a pull request

## Pull Request Process

1. Update the README.md with details of changes if needed
2. Add any new dependencies to the build files
3. Update the documentation
4. Get at least one code review from a maintainer
5. Once approved, a maintainer will merge your PR

## Coding Standards

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused
- Write comments for complex logic
- Use proper indentation (4 spaces)
- Include KDoc comments for public APIs

### Example of Good Code Style:
```kotlin
/**
 * Manages the audio routing for calls.
 * @param route The desired audio route
 * @return Result indicating success or failure
 */
suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit> {
    return try {
        platformAudio.switchRoute(route)
        CallResult.Success(Unit)
    } catch (e: Exception) {
        CallResult.Error(e)
    }
}
```

## Documentation

- Update API documentation for any changed code
- Include code examples for new features
- Update the README if adding new functionality
- Add comments explaining complex algorithms
- Document any breaking changes


## Need Help?

If you need help with anything:
1. Check the documentation
2. Search existing issues
3. Create a new issue
4. Make a pull request

Thank you for contributing to Ringlr! 🎉
