#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/vscode-extensions/$extension_id
server_id=${extension_id#vscode-}

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   echo "!!!This may slow down the build!!!"
fi

#cd ${sources}/../commons-vscode
#npm install

cd "$sources"

#npm install ../commons-vscode

base_version=`jq -r .version package.json`
if [ "$dist_type" = release ]; then
    echo -e "\n\n*Version: ${base_version}-RELEASE*" >> README.md
else
    if [ "$dist_type" = pre ]; then
        # for pre-release build, work the timestamp into package.json patch version. No minutes in the timestamp due VSCode pre-release lack of support for semver
        timestamp=`date -u +%Y%m%d%H`
        qualified_version=`echo $base_version | sed "s/\([0-9]\{1,\}.[0-9]\{1,\}.\)[0-9]\{1,\}/\1$timestamp/"`
        npm version ${qualified_version}
        echo -e "\n\n*Version: ${base_version}-PRE-RELEASE*" >> README.md
    else
        # for snapshot build, work the timestamp into package.json version qualifier
        timestamp=`date -u +%Y%m%d%H%M`
        qualified_version=${base_version}-${timestamp}
        npm version ${qualified_version}
        echo -e "\n\n*Version: ${qualified_version}*" >> README.md
    fi
fi

./scripts/preinstall.sh
npm install
#npm audit
if [ "$dist_type" = pre ]; then
    npm run vsce-pre-release-package
else
    npm run vsce-package
fi

# for release build we don't don't add version-qualifier to package.json
# So we must instead rename the file ourself to add a qualifier
if [ "$dist_type" == release ]; then
    vsix_file=`ls *.vsix`
    release_name=`git tag --points-at HEAD | grep ${extension_id}`
    echo "release_name=$release_name"
    if [ -z "$release_name" ]; then
        echo "Release Candidates must be tagged" >&2
        exit 1
    else
        mv $vsix_file ${release_name}.vsix
    fi
fi

cp *.vsix $workdir/out
server_jar=$workdir/sts4/headless-services/${server_id}-language-server/target/*-exec.jar
if [ -f $server_jar ]; then
    cp $server_jar $workdir/out/${server_id}-language-server-${base_version}-${timestamp}.jar
fi
