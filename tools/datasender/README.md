# Data sender configuration
The data sender expects a `datasender.conf` file in the current
directory (`tools/datasender`).

Data taken from DEBS 2012 Grand Challenge is extended by machine id
using the following command: 

- use `dos2unix` or alike to convert input csv
- append machine id (in this case '1' to csv: 

 `awk 'BEGIN { FS = OFS = "\t" } { $(NF+1) = 1; print $0 }' input.csv >
 output.csv`
