#!/usr/bin/env node
/**
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Writes the browser versions of the driver to README.md. The versions come from
// packages/playwright-core/browsers.json of the upstream checkout, which upstream keeps equal
// to the versions that the browsers report (utils/doclint/cli.js).
//
// Usage: node scripts/update_readme.js <upstream-checkout-dir>

const fs = require('fs');
const path = require('path');

const upstreamDir = process.argv[2];
if (!upstreamDir) {
  console.error('Usage: node scripts/update_readme.js <upstream-checkout-dir>');
  process.exit(1);
}

const browsersJSON = JSON.parse(fs.readFileSync(path.join(upstreamDir, 'packages', 'playwright-core', 'browsers.json'), 'utf8'));
const params = new Map();
for (const name of ['chromium', 'firefox', 'webkit']) {
  const browser = browsersJSON.browsers.find(b => b.name === name);
  if (!browser || !browser.browserVersion) {
    console.error(`ERROR: browsers.json has no browserVersion for "${name}"`);
    process.exit(1);
  }
  params.set(`${name}-version`, browser.browserVersion);
}

const readmePath = path.join(__dirname, '..', 'README.md');
let content = fs.readFileSync(readmePath, 'utf8');
content = content.replace(/<!-- GEN:([^ ]+) -->([^<]*)<!-- GEN:stop -->/ig, (match, p1) => {
  if (!params.has(p1)) {
    console.error(`ERROR: Invalid generate parameter "${p1}" in "${match}"`);
    process.exit(1);
  }
  return `<!-- GEN:${p1} -->${params.get(p1)}<!-- GEN:stop -->`;
});
fs.writeFileSync(readmePath, content);
