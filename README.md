# cordova-plugin-xminnov-IVF-RU01
1. install this plugin 
2. How to use 
```typescript
import ...
declare var Xminnovrfidreaderaudiojack:any
@Component({...})
...
    platform.ready().then(() => {
 
      Xminnovrfidreaderaudiojack.registerService((data)=>{
        console.log("registerService")
      })

      Xminnovrfidreaderaudiojack.onDeviceStatusChange(
        (data)=>{
          console.log("onDeviceStatusChange")
          console.log(data)
          if(data == "Device connected."){
            Xminnovrfidreaderaudiojack.start()
          }else{
            Xminnovrfidreaderaudiojack.stop()
            Xminnovrfidreaderaudiojack.unRegisterService()
          }
        },
        (msg)=>{
          console.log('onDeviceStatusChange callback successfully registered: ' + msg)
        },
        (err)=>{
          console.log('Error registering onDeviceStatusChange callback: ' + err)
        }
      )

      Xminnovrfidreaderaudiojack.onScannerReceive(
        (data)=>{
          console.log("onScannerReceive")
          alert(data)
        },
        (msg)=>{
          console.log('onScannerReceive callback successfully registered: ' + msg)
        },
        (err)=>{
          console.log('Error registering onScannerReceive callback: ' + err)
        }
      ) 
  })
