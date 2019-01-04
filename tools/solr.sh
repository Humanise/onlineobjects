#!/usr/bin/env bash

# 8984 is the local port
docker run --name onlineobjects_solr -d -p 8984:8983 -t solr