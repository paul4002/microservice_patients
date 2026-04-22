#!/usr/bin/env bash
# Instala los git hooks del proyecto.
set -e

cd "$(git rev-parse --show-toplevel)"

git config core.hooksPath .githooks
chmod +x .githooks/*
echo "Git hooks installed. core.hooksPath => .githooks"
