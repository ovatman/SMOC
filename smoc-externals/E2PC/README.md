# An Enhanced 2PC Algorithm

This repository includes the thesis study I have done in Istanbul Technical University (ITU) with the guidance of my proffessor Tolga Ovatman. We have seperated the steps of the 2PC protocol in an environment where each incoming transitions coming from clients have a precedence. Then this design is integrated into a replicated state machine, and tested on both local machines, and on Amazon Web Services (AWS).

Since the code base needs to be open before the relase of the paper itself. I will be putting the links for the thesis, and the published research paper later on.

## Related Research Paper
Enhancing Two Phase-Commit Protocol for Replicated State Machines
https://ieeexplore.ieee.org/document/9092340

## How to Run

**Disclaimer**, the priority generator currently provided in this repository has the possibility of generating equal values. Which **will** result in servers to print an error message if an equality situation happens. In that case, just restart the experiment again. If you want, an easy way to distinguish equal priorities could be to check GUID values.

Running the application for test purposes is very easy. There are two ways you may want to do this. First one is to run it locally, which I'd suggest, but may not be very optimal depending on the number of parallel cores you have (Virtual Cores Should not be accounted for). Second one is to run on a cloud environment, I will give an example for running it on AWS.

**Before starting, it is required to decide on how many server-client combos you want to run. Which you will be defining under WANSettings.json file. Each server added there represents a couple, and each server should have the same ip to connect to.Because they will be putting up their channels on to the same server.** But considering that the communication is not optimal, you may want to change it, then depending on your design, the settings could be different.

**Also since you will want to change the ips on the WANSettings file, you should just clone the branch and do not use master.**

### 1. Testing Locally

First you should install docker on your system. This is required for easy RabbitMQ deployment.

A really simple way to run a rabbitMQ broker node after installing docker is:

    docker run -d -h node-1.rabbit --name rabbit -p "4369:4369" -p "5672:5672" -p "15672:15672" -p "25672:25672" -p "35197:35197" -e "RABBITMQ_USE_LONGNAME=true" -e "RABBITMQ_LOGS=/var/log/rabbitmq/rabbit.log" -v /data:/var/lib/rabbitmq -v /data/logs:/var/log/rabbitmq rabbitmq:3-management

This will open up the ports for your RabbitMQ container in order for our clients, and servers to use. It will also enable the management interface, where you can see the message traffic.

After you made sure that rabbitmq is up and running, you can simply run the commands which will start the application. I have added sample bash scripts for both of the algorithms, which runs however many server-client combos you have defined in the previous section. You can find them in the .bat files.

### 2. Testing On Amazon Web Services (AWS)

Similar to testing locally, testing the application on AWS is also easy. Only hurdle might be if you dont have the free tier choice, then you may need to pay up. 

- Open up the EC2 interface. 
- Click "Launch Instance" button.
- Choose the machine type you will be running everything on (there is a little button with **free tier only** if you want)
- Choose the instance type 
- Under Configure Instance tab:
    - Number of Instances: choose the number of couples defined in settings file, if you want to run RabbitMQ on another machine, you may want to choose a number larger than the number of couples.
    - Input the bash script which will install everything to the **User Data** input under the **Advanced Details** tab. **TODO**:: put bash script here after repo is public.
    - You dont have to do anything to rest of the settings here if you dont want.
- Store and tags is also not required to be modified
- Configure Security Group
    - You will be opening up the ports here so your machines can communicate.
    - An example can be seen on the image. You need to open up 22, 80, 4369, 5672, 8080, 15672, 25672, 35197 ports.
    - Later on, you can use this same definition on other server setups if you want.

![Alt text](/images/Ports.png "Title")    

- Finally, before launching the machines, do note that since you have your ips after launching the machines, you may want to change your WANSettings file and push it while the deployment is still going on for convenience. Otherwise, you will apply a push and pull for your ip settings.

After the servers are up and running, you can use your favourite ssh client to connect to the machines and run the application.

## Result Files

During testing, the output of the experiments can be found under the Output directory.
