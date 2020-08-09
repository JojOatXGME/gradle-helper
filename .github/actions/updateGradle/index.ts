import * as core from '@actions/core';
import {exec} from "@actions/exec";
import fetch from "node-fetch";

interface VersionInfo {
    version: string;
    broken: boolean;
}

async function main() {
    const stage = core.getInput("stage");
    const latestVersion = await core.group(
        `Fetch the latest version of stage ‘${stage}’`,
        () => fetchLatestVersion(stage));

    if (latestVersion.broken) {
        core.warning(`Version ‘${latestVersion.version}’ is marked as broken`);
    }

    await core.group(
        `Update Gradle to version ‘${latestVersion.version}’`,
        () => updateGradle(latestVersion.version));
}

async function fetchLatestVersion(stage: string): Promise<VersionInfo> {
    const latestVersion =
        await fetch(`https://services.gradle.org/versions/${stage}`)
            .then(response => response.json());

    const prettyJson = JSON.stringify(latestVersion, null, 2);
    core.info(`Fetched version: ${prettyJson}`);
    if (typeof latestVersion.version !== "string" ||
        typeof latestVersion.broken !== "boolean") {
        throw new Error("Invalid format");
    }

    return latestVersion;
}

async function updateGradle(version: string) {
    await exec("./gradlew", [
        "wrapper",
        `--gradle-version=${version}`,
    ]);
    // The first run updates Gradle to the latest release but does not
    // update the Wrapper. We need to execute the task a second time to
    // also update the Wrapper itself.
    await exec("./gradlew", [
        "wrapper",
    ]);
}

main().catch(reason => {
    core.setFailed(reason instanceof Error ? reason : new Error(reason));
});
