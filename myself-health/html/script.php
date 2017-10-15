<html>
 <head>
 </head>
 <body>
 <h1>PHP connect to MySQL</h1>
 Some text.<br><br>

<?php
// Connect to database server
mysql_connect("asdfasdfas", "user", "12user34") or die (mysql_error ());

// Select database
mysql_select_db("epi") or die (mysql_error());

// SQL query
$strSQL = "SELECT * FROM eag_sims";

// Execute the query (the recordset $rs contains the result)
$rs = mysql_query($strSQL);

// Loop the recordset $rs
// Each row will be made into an array ($row) using mysql_fetch_array
while($row = mysql_fetch_array($rs)) {

   // Write the value of the column FirstName (which is now in the array $row)
  echo $row['id'] . ;

  }

// Close the database connection
mysql_close();
?>

test text.<br>

</body>
</html>