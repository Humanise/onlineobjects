#!/usr/bin/env bash

# Simulate a spider

wget --spider --recursive --no-parent --user-agent="Mozilla/5.0 (compatible; MyWebSpider/1.0)" --reject "jpg,jpeg,gif,png,css,js,ico,svg" -o spider.log http://words.onlineobjects.com