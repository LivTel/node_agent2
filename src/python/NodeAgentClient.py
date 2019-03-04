#import suds
from suds.client import Client
import logging
import argparse

class NodeAgentClient:

    def initialise_web_service(self,hostname,port_number,username,password):
        """Initialise the web service client object."""
        # With authentication
        authenticationHeader = {"Username" : username,"Password" : password}
        self.client = Client(url="http://"+hostname+":"+port_number+"/node_agent2/node_agent?wsdl", headers=authenticationHeader)

    def ping(self):
        """Invoke the Node agent web service ping."""
        return self.client.service.ping()

    def handle_rtml(self,rtml_document_string):
        """Invoke the Node agent web service handle_rtml."""
        return self.client.service.handle_rtml(rtml_document_string)

    def add_arguments(self,parser):
        """Add command line arguments to an optparse parser."""
        parser.add_argument(
            '--hostname',
            default='ltproxy',
            help='Specify the hostname hosting the Node Agent web service container')
        parser.add_argument(
            '--port_number',
            default='8080',
            help='Specify the port number the Node Agent web service container is running on')
        parser.add_argument(
            '--username',
            default='eng',
            help='Specify the username to authenticate the web service call with.')
        parser.add_argument(
            '--password',
            default='none',
            help='Specify the password to authenticate the web service call with.')
        parser.add_argument(
            '--ping',
            action="store_true",
            help='Call the ping web service.')
        parser.add_argument(
            '--handle_rtml',
            type=argparse.FileType('r'),
            help='Call the handle_rtml web service with the specified RTML filename as input.')
        parser.add_argument(
            '--output',
            type=argparse.FileType('w'),
            help='Save the returned RTML from the handle_rtml web service in the specified filename..')

    def load_file(self,filename):
        '''Load an RTML filename from the specified filename and return it as a string.'''
        return_string = filename.read()
        filename.close()
        return return_string

    def save_file(self,filename,content_string):
        '''Save the specified content_string into the specified filename'''
        filename.write(content_string)
        filename.close()

if(__name__ == "__main__"):
    # arguments parser
    parser = argparse.ArgumentParser(description='Node Agent Client.')
    client = NodeAgentClient()
    client.add_arguments(parser)
    args = parser.parse_args()
    print "Initialising web service to host "+args.hostname+":"+args.port_number+"."
    print "Using username "+args.username+" and password "+args.password+"."
    client.initialise_web_service(args.hostname,args.port_number,args.username,args.password)
    #logging.basicConfig(level=logging.DEBUG)
    if args.ping:
        print "Invoking NodeAgent ping() method."
        return_string = client.ping()
        print "Returned message from NodeAgent ping() method was:" + return_string + ".\n"
    elif args.handle_rtml:
        print "Loading RTML from file:"+ args.handle_rtml.name
        rtml_string = client.load_file(args.handle_rtml)
        #print "Invoking NodeAgent handle_rtml() method with RTML document:"+ rtml_string
        print "Invoking NodeAgent handle_rtml() method."
        return_string = client.handle_rtml(rtml_string)
        if args.output:
            print "Saving returned RTML document to file:"+ args.output.name
            client.save_file(args.output,return_string)
        else:
            print "Returned RTML from handle_rtml method was:" + return_string + "\n"
    else:
        print "No method to invoke:Please specify --ping or --handle_rtml <rtml_filename>"

