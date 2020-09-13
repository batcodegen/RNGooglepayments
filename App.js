import React, { Component } from "react";
import {
  Platform,
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Modal,
  Image,
  NativeModules
} from "react-native";
import { createStackNavigator, createAppContainer } from "react-navigation";
import Share, { ShareSheet, Button } from "react-native-share";
import { captureScreen } from "react-native-view-shot";
import RNFS from "react-native-fs";
import { PaymentRequest } from "react-native-payments";

const GPay = NativeModules.GPay;

const instructions = Platform.select({
  ios: "Press Cmd+R to reload,\n" + "Cmd+D or shake for dev menu",
  android:
    "Double tap R on your keyboard to reload,\n" +
    "Shake or press menu button for dev menu"
});
const image1 = `data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHUAAAB1CAIAAAD/ZjnrAAAACXBIWXMAAAsTAAALEwEAmpwYAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAAECUlEQVR42mJkAAJGJoZRQHXw/x+QAAggIBoNXNoAJmYgARBAo+FLE6DIIn/HYK4euzJAAI2GL60C96vwJWD4AgTQaOBSP3B369RJPjaFcAECaDR8aRi4QAAQQKPhS8PABQKAABoNXxoGLhAABNBo+NIwcIEAIIBGw5eGgQsEAAE0Gr40DFwgAAig0fClYeACAUAAjYYvDQMXCAACaDR8aRi4QAAQQKPhS8PABQKAABoNXxoGLhAABNBo+NIwcIEAIIBGw5eGgQsEAAE0Gr40DFwgAAig0fClYeACAUAAjYYvDQMXCAACaDR8aRi4QAAQQKPhS8PABQKAABoNXxoGLhAABNBo+NIwcIEAIIBGw5eGgQsEAAE0Gr40DFwgAAig0fClYeACAUAAjYYvDQMXCAACaDR8aRi4QAAQQKPhS8PABQKAAGIaDVzaBS4QAAQQ02jg0i5wgQAggJhGA5emtgAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEENNo4NIUAAQQ02jg0hQABBDTaODSFAAEEDB8/zEwMY8GLo0AQAAxQih9DuXRsKA6+PT7J0CAAQDSMUzAOFpaOwAAAABJRU5ErkJggg==`;
let shareOptions = {
  title: "Share",
  message: "Test out app",
  url: "http://mytestapp.com",
  subject: "Wow! Did you see that?" //  for email
};

const METHOD_DATA = [
  {
    supportedMethods: ["android-pay"],
    data: {
      supportedNetworks: ["visa", "mastercard", "amex"],
      currencyCode: "USD",
      environment: "TEST", // defaults to production
      paymentMethodTokenizationParameters: {
        tokenizationType: "NETWORK_TOKEN",
        parameters: {
          publicKey: "your-pubic-key"
        }
      }
    }
  }
];

const DETAILS = {
  id: "basic-example",
  displayItems: [
    {
      label: "Movie Ticket",
      amount: { currency: "USD", value: "15.00" }
    }
  ],
  total: {
    label: "Merchant Name",
    amount: { currency: "USD", value: "15.00" }
  }
};

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      showModal: false,
      imageUri: "",
      shareUri: ""
    };
  }

  componentDidMount() {
    GPay.isReadyToPay((error, success) => {
      if (success) {
        console.log("---------call success------> ", success);
      } else if (error) {
        console.log("---------call failed------> ", error);
      }
    });
  }

  showPaymentSheet = () => {
    PaymentRequest.show();
  };

  onCancel = () => {
    this.setState({ visible: false });
  };
  onOpen = () => {
    this.setState({ visible: true });
  };

  onShare = () => {
    const shareOpt = {
      title: "Share file",
      url: image1
    };
    return Share.open(shareOpt);
  };

  toggleModal = () => {
    this.setState({ showModal: !this.state.showModal });
  };

  takeScreenshot = () => {
    captureScreen({
      format: "png",
      quality: 0.8
    }).then(
      uri => {
        RNFS.readFile(uri, "base64")
          .then(res => {
            let base64String = `data:image/png;base64,${res}`;
            this.setState({ imageUri: uri, shareUri: base64String });
          })
          .catch(err => console.log("--error image read---", err));
        this.toggleModal();
      },
      error => {
        console.log("---error in screen capture--", error);
      }
    );
  };

  shareScreenshot = () => {
    const shareOpt = {
      title: "Share file",
      url: this.state.shareUri,
      message: "shared from cell"
    };
    return Share.open(shareOpt);
  };

  showGooglePaySheet = () => {
    GPay.showPayment(
      {
        gatewayIdentifier: "example",
        gatewatMerchantId: "exampleGatewayMerchantId",
        price: "20",
        currencyCode: "INR",
        merchantName: "Example Merchant"
      },
      (error, success) => {
        if (success) {
          console.log("payment success-->", success);
        } else if (error) {
          console.log("payment failed-->", error);
        }
      }
    );
  };

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to ADMOB POC!</Text>
        <Text style={styles.instructions}>{instructions}</Text>
        <TouchableOpacity
          onPress={() => {
            Share.open(shareOptions);
          }}
        >
          <Text>Share</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.onOpen}>
          <Text>Share more</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.onShare}>
          <Text>Share image</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.takeScreenshot}>
          <Text>Take screenshot</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.showPaymentSheet}>
          <Text>Pay</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.showGooglePaySheet}>
          <Image
            source={require("./gpay.png")}
            style={{ width: 80, height: 50 }}
          />
        </TouchableOpacity>
        <ShareSheet visible={this.state.visible} onCancel={this.onCancel}>
          <Button
            onPress={() => {
              Share.shareSingle(
                Object.assign(shareOptions, {
                  social: "email"
                })
              );
            }}
          >
            Share More
          </Button>
        </ShareSheet>
        <Modal
          visible={this.state.showModal}
          onRequestClose={this.toggleModal}
          transparent={true}
        >
          <View
            style={{
              flex: 1,
              backgroundColor: "rgba(255,255,255,0.7)",
              alignItems: "center",
              justifyContent: "center"
            }}
          >
            <View>
              {this.state.imageUri && this.state.shareUri ? (
                <Image
                  source={{ uri: this.state.imageUri }}
                  style={{ width: 300, height: 300 }}
                  resizeMode={"cover"}
                />
              ) : null}
            </View>
            <TouchableOpacity onPress={this.shareScreenshot}>
              <Text>Share screenshot</Text>
            </TouchableOpacity>
          </View>
        </Modal>
      </View>
    );
  }
}

const MainApp = () => <AppNavigator />;

const AppNavigator = createAppContainer(
  createStackNavigator({
    Home: {
      screen: App,
      path: "admob/"
    }
  })
);

export default MainApp;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  }
});
