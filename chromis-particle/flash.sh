#!/bin/sh

cat particle.include | xargs particle flash $1 --target 0.6.1
