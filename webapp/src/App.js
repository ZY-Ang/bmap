import React from "react";
import { VectorMap } from '@south-paw/react-vector-maps';
import world from './world.json';
import './App.css';
import styled from 'styled-components';

import {
    subscribeDataForWord,
    subscribeInvocationDistribution,
    unsubscribeDataForWord,
    unsubscribeInvocationDistribution
} from "./database";


const Map = styled.div`
    margin: 1rem auto;
    width: 1000px;
  svg {
    stroke: black;

    // All layers are just path elements
    path {
      fill: grey;
      cursor: pointer;
      outline: none;

      // When a layer is hovered
      &:hover {
        fill: rgba(168,43,43,0.83);
      }

      // When a layer is focused.
      &:focus {
        fill: rgba(168,43,43,0.6);
      }

      // When a layer is 'checked' (via checkedLayers prop).
      &[aria-checked='true'] {
        fill: rgba(56,43,168,1);
      }

      // When a layer is 'selected' (via currentLayers prop).
      &[aria-current='true'] {
        fill: rgba(56,43,168,0.83);
      }

      // You can also highlight a specific layer via it's id
      &[id = ${props => props.theme}] {
        fill: #ff1919;
      }
    }
  }
`;


class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            word: "hello",
            previousWord: "hello",
            invocationDistribution: {},
            wordDistribution: null
        };
    }

    componentDidMount() {
        this.invocationDistributionListener = snapshot => this.setState({
            invocationDistribution: snapshot.val()
        });
        subscribeInvocationDistribution(this.invocationDistributionListener);
        this.getWord();
    }

    getWord = e => {
        if (e) {
            e.preventDefault();
        }
        const { word } = this.state;
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
            this.setState({ previousWord: word });
        }
        this.wordDistributionListener = snapshot => this.setState(state => {
            const { invocationDistribution } = this.state;
            let wordDistribution = snapshot.val();
            if (!!wordDistribution && wordDistribution.constructor === Object && Object.keys(invocationDistribution).length > 0) {
                Object.keys(wordDistribution).map(key => (wordDistribution[key] /= invocationDistribution[key]));
                return { wordDistribution };
            } else return { wordDistribution: null };
        });
        subscribeDataForWord(word, this.wordDistributionListener);
    };

    componentWillUnmount() {
        if (!!this.invocationDistributionListener) {
            unsubscribeInvocationDistribution(this.invocationDistributionListener);
        }
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
        }
    }

    changecountryCodes(data) {
        if (data != null) {
            const array = [];
            for (var x of Object.keys(data)) {
                array.push(x.toLowerCase());
            }
            console.log(array);
            return array;
        }
        return [];
    }

    colorize(floatnumber) {
        if (floatnumber > 0 && floatnumber < 0.1) {
            return "ffb2b2";
        }else if (floatnumber >= 0.1 && floatnumber < 0.4){
            return "ff6666";
        }else {
            return "ff1919";
        }
    }

    render() {
        console.log(this.state.wordDistribution)
        return (
            <div className="App">
                <form
                    style={{
                        position: 'absolute',
                        width: '100%',
                        height: '30%',
                        display: 'flex',
                        justifyContent: 'left',
                        alignItems: 'center'
                    }}
                    onSubmit={this.getWord}
                >
                    <input
                        type="text"
                        placeholder="hello"
                        value={this.state.word}
                        onChange={e => this.setState({ word: e.target.value })}
                    />
                </form>
                <Map theme={this.changecountryCodes(this.state.wordDistribution).map(function (name) {
                    console.log(name); return name;
                })}>
                    <VectorMap {...world}
                    />
                </Map>
            </div>
        );
    }
}

export default App;
