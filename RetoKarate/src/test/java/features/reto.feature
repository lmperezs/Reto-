Feature:

  Background:
    * def bucketname = 's3pruebalaura'
    * def region = aws.region
    * def awsconnect = Java.type('aws.AwsConnect')
    * def keyName = 'listaUsuarios.txt'
    * def contenido = read('listaUsuarios.txt')

  @ListObject
  Scenario: listobject
    Given def res = awsconnect.listS3(bucketname,region)
    When print res
    Then match res == '#notnull'


  @VerifyObject
  Scenario: verify object
    Given def res = awsconnect.verifyObject(region,bucketname,keyName)
    When print res
    Then match res == true


  @DownloadFile
  Scenario: dowloand file
    Given json res = awsconnect.DowloandFile(region,bucketname,keyName)
    When print res
    * json auxiliar = contenido
    * print auxiliar
    Then match res == auxiliar

  # auxiliar es el contenido del Json de lo que esta en local ,
  # res es el contenido json de lo que esta en el bucket




