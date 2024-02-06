#!/bin/bash
# Purpose of this script is to upload heapdump and GC log files to a S3 bucket.
timestamp()
{
date +"%Y-%m-%d %T"
}
LOG_FILE="/var/log/s3_upload.log"
exec > >(tee -a $LOG_FILE) # directs stdout to log file
exec 2>&1 # and also to console
ec2InstanceId=`hostname`

#echo "Printing all the environment variables"
#printenv

NOW=$(date +"%Y%m%d%H%M%S")
expirationDate=$(date -d $(date +"%Y/%m/%"d)+" 30 days" +%Y/%m/%d)
echo "$(timestamp): look for heap dumps to upload "
accountNumber=`aws sts get-caller-identity --output text --query 'Account'`
echo $accountNumber
bucketName="tfcom-monitoring-$SPRING_PROFILES_ACTIVE"
echo $bucketName

cd /var/log/

for gc_file in gc-*.log
do
echo "$(timestamp): Processing $gc_file file..."
gzip $gc_file
aws s3 cp ${gc_file}.gz "s3://${bucketName}/gc/${ec2InstanceId}-${gc_file}.gz"
# rm ${gc_file}.gz
echo "$(timestamp): upload dump successfully"
done
echo "$(timestamp): done gc log dump loop"

for hprof_file in *.hprof
do
echo "$(timestamp): Processing $hprof_file file..."
gzip $hprof_file
aws s3 cp ${hprof_file}.gz "s3://${bucketName}/heapdump/${ec2InstanceId}-${hprof_file}.gz"
# rm ${hprof_file}.gz
echo "$(timestamp): upload dump successfully"
done
echo "$(timestamp): done heap dump loop"

