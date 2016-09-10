# Instance should be terminated
#gcloud compute instances set-machine-type tron-bot --zone us-west1-b --machine-type n1-standard-1
#gcloud compute instances set-machine-type tron-bot --zone us-west1-b --machine-type n1-highcpu-32

# Start
gcloud compute instances start tron-bot --zone us-west1-b && \

# Copy executable
gcloud compute copy-files ~/projects/tron-bot/java/out/artifacts/tron.jar/ tron-bot:~/ --zone us-west1-b && \

# Run and pipe to file
gcloud compute ssh tron-bot --zone us-west1-b --command "java -Xmx 8192 -jar ~/tron.jar > out.txt" && \

# Copy output file back
gcloud compute copy-files tron-bot:~/out.txt ~/projects/tron-bot/googleCompute --zone us-west1-b && \

# Close down
gcloud compute instances stop tron-bot --zone us-west1-b
