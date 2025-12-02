# Copyright 2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM node:20.5.0-alpine@sha256:11087abe911baf2fd7e34192f4598bf7e438239e9914f5b7ecda5fb5a7b1a2dd as base

FROM base as builder

ARG SEALIGHTS_TOKEN
ARG BRANCH
ARG BUILD_NAME=0.4.3
ENV SEALIGHTS_NODE_AGENT=slnodejs
# Some packages (e.g. @google-cloud/profiler) require additional
# deps for post-install scripts
RUN apk add --update --no-cache \
    python3 \
    make \
    g++

# Enable corepack and install pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /usr/src/app

#COPY package*.json ./
COPY . .
RUN pnpm install --prod
#Install the agent (Not a url)
RUN pnpm add ${SEALIGHTS_NODE_AGENT}
#Run config to get build session id
RUN  pnpm exec slnodejs config --token ${SEALIGHTS_TOKEN} --appname "paymentservice" --branch ${BRANCH} --build ${BUILD_NAME}
#Scan the code
RUN pnpm exec slnodejs scan --token ${SEALIGHTS_TOKEN} --buildsessionidfile buildSessionId --workspacepath "." --scm none --es6Modules

FROM base

# Enable corepack and install pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /usr/src/app
#Copy the build session id file
COPY --from=builder /usr/src/app/buildSessionId .
COPY --from=builder /usr/src/app/node_modules ./node_modules

COPY . .
ENV NODE_DEBUG=sl
EXPOSE 50051
#Run code with sealights
CMD  pnpm exec slnodejs run --token "${SL_TOKEN}" --buildsessionidfile buildSessionId --labid "${SL_LAB_ID}" --workspacepath . index.js
