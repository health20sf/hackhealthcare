init:
	npm install --prefix ext/react_app gh-pages jquery react react-dom react-scripts

build:
	npm run --prefix ext/react_app build

run:
	hackhealthcare-personalized/index.py -v

production:
	sudo touch /etc/authbind/byport/80
	sudo chown "${SUDO_UID}":"${SUDO_GID}" /etc/authbind/byport/80
	sudo chmod 755 /etc/authbind/byport/80
	authbind --depth 2 hackhealthcare-personalized/index.py --port 80

deploy:
	rsync -avzzr -e "ssh -i ~/Downloads/hackhealthcare-personalized.pem" --exclude="*.swp" --exclude=".git" --exclude="node_modules" . ubuntu@ec2-34-214-153-134.us-west-2.compute.amazonaws.com:~/hackhealthcare-personalized
#	scp -i ~/Downloads/hackhealthcare-personalized.pem hackhealthcare-personalized/*.py ubuntu@ec2-34-214-153-134.us-west-2.compute.amazonaws.com:~
