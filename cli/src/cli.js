import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let lastCommand

// var colors = require('colors/safe')

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
.mode('connect <username>')
.delimiter(cli.chalk['green']('connected>'))
.init(function (args, callback) {
  username = args.username
  server = connect({ host: 'localhost', port: 8080 }, () => {
    server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
    callback()
  })

  server.on('data', (buffer) => {
    // this.log(Message.fromJSON(buffer).toString())
    const m = Message.fromJSON(buffer)
    if (Message.fromJSON(buffer).command === 'connect' || Message.fromJSON(buffer).command === 'disconnect') {
      this.log(cli.chalk['red'](m.toString()))
    } else if (Message.fromJSON(buffer).command === 'echo') {
      this.log(cli.chalk['red'](m.toString()))
    } else if (Message.fromJSON(buffer).command === 'broadcast') {
      this.log(cli.chalk['red'](m.toString()))
    } else if (Message.fromJSON(buffer).command === 'users') {
      this.log(cli.chalk['cyan'](m.toString()))
    } else if (Message.fromJSON(buffer).command.charAt(0) === '@') {
      this.log(cli.chalk['red'](m.toString()))
    }
  })

  server.on('end', () => {
    cli.exec('exit')
  })
})
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input, /[^\s]+/g)
    let contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      lastCommand
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      lastCommand
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      lastCommand
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.charAt(0) === '@') {
      lastCommand
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command !== undefined) {
      lastCommand
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
