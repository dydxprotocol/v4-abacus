#!/bin/sh

echo 

vercomp () {
    if [[ $1 == $2 ]]
    then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    # fill empty fields in ver1 with zeros
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
    do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++))
    do
        if [[ -z ${ver2[i]} ]]
        then
            # fill empty fields in ver2 with zeros
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]}))
        then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]}))
        then
            return 2
        fi
    done
    return 0
}

# Defining a temporary directory for cloning
TMP_DIR=$(mktemp -d)

curl https://raw.githubusercontent.com/dydxprotocol/v4-abacus/main/build.gradle.kts > $TMP_DIR/build.gradle.kts

# search for the first line that starts with "version" in build.gradle.kts
# get the value in the quotes
VERSION=$(grep "^version = " build.gradle.kts | sed -n 's/version = "\(.*\)"/\1/p')

REPO_VERSION=$(grep "^version = " $TMP_DIR/build.gradle.kts | sed -n 's/version = "\(.*\)"/\1/p')

# call the version comparison function

vercomp $REPO_VERSION $VERSION
    case $? in
        0) SHOULD_BUMP=true ;;
        1) SHOULD_BUMP=true ;;
        2) SHOULD_BUMP=false ;;
    esac

if [ $SHOULD_BUMP == false ]; then
    echo "Repo version < PR version... No need to bump."
    exit -1
fi

# increment the version number
NEW_VERSION=$(echo $REPO_VERSION | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')

#if NEW_VERSION is not empty, replace the version in build.gradle.kts
if [ -n "$NEW_VERSION" ]; then
  sed -i '' "s/version = \"$VERSION\"/version = \"$NEW_VERSION\"/" build.gradle.kts
  echo "Version bumped to $NEW_VERSION"
fi

exit 0
