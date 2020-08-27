#!/bin/bash
## resolve folder of this script, following all symlinks,
## http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done
readonly SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"

pushd $SCRIPT_DIR/test/reproducers
if [ -e cryptoTest/cryptotest ] ; then
  cd cryptoTest
  git pull -v
else
  git clone https://github.com/judovana/CryptoTest.git
  mv -vf CryptoTest/* CryptoTest/.git* cryptoTest
  rmdir -v CryptoTest
fi
popd

pushd $SCRIPT_DIR/test/reproducers/ssl-tests
if [ -e ssl-tests ] ; then
    cd ssl-tests
    git pull -v
else
    git clone "https://github.com/zzambers/ssl-tests.git"
fi
popd
