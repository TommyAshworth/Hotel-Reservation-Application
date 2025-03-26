FROM ubuntu:latest
LABEL authors="tommy"

ENTRYPOINT ["top", "-b"]