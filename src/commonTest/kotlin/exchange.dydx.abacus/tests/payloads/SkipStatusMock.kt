package exchange.dydx.abacus.tests.payloads

class SkipStatusMock {

    internal val depositFromEthEthSubmitted = """{
  "transfers": [
      {
          "state": "STATE_SUBMITTED",
          "transfer_sequence": [],
          "next_blocking_transfer": null,
          "transfer_asset_release": null,
          "error": null
      }
  ],
  "state": "STATE_SUBMITTED",
  "transfer_sequence": [],
  "next_blocking_transfer": null,
  "transfer_asset_release": null,
  "error": null,
  "status": "STATE_SUBMITTED"
  }"""

    internal val depositFromEthEthPending = """{
  "transfers": [
      {
          "state": "STATE_PENDING",
          "transfer_sequence": [
              {
                  "axelar_transfer": {
                      "from_chain_id": "1",
                      "to_chain_id": "osmosis-1",
                      "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                      "state": "AXELAR_TRANSFER_PENDING_CONFIRMATION",
                      "txs": {
                          "contract_call_with_token_txs": {
                              "send_tx": {
                                  "chain_id": "1",
                                  "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                                  "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                              },
                              "gas_paid_tx": {
                                  "chain_id": "1",
                                  "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                                  "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                              },
                              "confirm_tx": null,
                              "approve_tx": null,
                              "execute_tx": null,
                              "error": null
                          }
                      },
                      "axelar_scan_link": "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                      "src_chain_id": "1",
                      "dst_chain_id": "osmosis-1"
                  }
              }
          ],
          "next_blocking_transfer": {
              "transfer_sequence_index": 0
          },
          "transfer_asset_release": null,
          "error": null
      }
  ],
  "state": "STATE_PENDING",
  "transfer_sequence": [
      {
          "axelar_transfer": {
              "from_chain_id": "1",
              "to_chain_id": "osmosis-1",
              "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
              "state": "AXELAR_TRANSFER_PENDING_CONFIRMATION",
              "txs": {
                  "contract_call_with_token_txs": {
                      "send_tx": {
                          "chain_id": "1",
                          "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                          "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                      },
                      "gas_paid_tx": {
                          "chain_id": "1",
                          "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                          "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                      },
                      "confirm_tx": null,
                      "approve_tx": null,
                      "execute_tx": null,
                      "error": null
                  }
              },
              "axelar_scan_link": "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
              "src_chain_id": "1",
              "dst_chain_id": "osmosis-1"
          }
      }
  ],
  "next_blocking_transfer": {
      "transfer_sequence_index": 0
  },
  "transfer_asset_release": null,
  "error": null,
  "status": "STATE_PENDING"
}"""

    internal val testPayload = """{
    "transfers": [
        {
            "state": "STATE_PENDING",
            "transfer_sequence": [
                {
                    "axelar_transfer": {
                        "from_chain_id": "1",
                        "to_chain_id": "osmosis-1",
                        "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                        "state": "AXELAR_TRANSFER_PENDING_RECEIPT",
                        "txs": {
                            "contract_call_with_token_txs": {
                                "send_tx": {
                                    "chain_id": "1",
                                    "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                                    "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                                },
                                "gas_paid_tx": {
                                    "chain_id": "1",
                                    "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                                    "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                                },
                                "confirm_tx": {
                                    "chain_id": "axelar-dojo-1",
                                    "tx_hash": "282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0",
                                    "explorer_link": "https://mintscan.io/axelar/tx/282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0"
                                },
                                "approve_tx": null,
                                "execute_tx": null,
                                "error": null
                            }
                        },
                        "axelar_scan_link": "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                        "src_chain_id": "1",
                        "dst_chain_id": "osmosis-1"
                    }
                }
            ],
            "next_blocking_transfer": {
                "transfer_sequence_index": 0
            },
            "transfer_asset_release": null,
            "error": null
        }
    ],
    "state": "STATE_PENDING",
    "transfer_sequence": [
        {
            "axelar_transfer": {
                "from_chain_id": "1",
                "to_chain_id": "osmosis-1",
                "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                "state": "AXELAR_TRANSFER_PENDING_RECEIPT",
                "txs": {
                    "contract_call_with_token_txs": {
                        "send_tx": {
                            "chain_id": "1",
                            "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                            "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                        },
                        "gas_paid_tx": {
                            "chain_id": "1",
                            "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                            "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                        },
                        "confirm_tx": {
                            "chain_id": "axelar-dojo-1",
                            "tx_hash": "282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0",
                            "explorer_link": "https://mintscan.io/axelar/tx/282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0"
                        },
                        "approve_tx": null,
                        "execute_tx": null,
                        "error": null
                    }
                },
                "axelar_scan_link": "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                "src_chain_id": "1",
                "dst_chain_id": "osmosis-1"
            }
        }
    ],
    "next_blocking_transfer": {
        "transfer_sequence_index": 0
    },
    "transfer_asset_release": null,
    "error": null,
    "status": "STATE_PENDING"
}"""

    internal val depositFromEthEthSuccess = """
        {
          "transfers": [
            {
              "state": "STATE_COMPLETED_SUCCESS",
              "transfer_sequence": [
                {
                  "axelar_transfer": {
                    "from_chain_id": "1",
                    "to_chain_id": "osmosis-1",
                    "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                    "state": "AXELAR_TRANSFER_SUCCESS",
                    "txs": {
                      "contract_call_with_token_txs": {
                        "send_tx": {
                          "chain_id": "1",
                          "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                          "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                        },
                        "gas_paid_tx": {
                          "chain_id": "1",
                          "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                          "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                        },
                        "confirm_tx": {
                          "chain_id": "axelar-dojo-1",
                          "tx_hash": "282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0",
                          "explorer_link": "https://mintscan.io/axelar/tx/282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0"
                        },
                        "approve_tx": null,
                        "execute_tx": {
                          "chain_id": "osmosis-1",
                          "tx_hash": "2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0",
                          "explorer_link": "https://www.mintscan.io/osmosis/transactions/2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0"
                        },
                        "error": null
                      }
                    },
                    "axelar_scan_link": "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                    "src_chain_id": "1",
                    "dst_chain_id": "osmosis-1"
                  }
                },
                {
                  "ibc_transfer": {
                    "from_chain_id": "osmosis-1",
                    "to_chain_id": "noble-1",
                    "state": "TRANSFER_SUCCESS",
                    "packet_txs": {
                      "send_tx": {
                        "chain_id": "osmosis-1",
                        "tx_hash": "2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0",
                        "explorer_link": "https://www.mintscan.io/osmosis/transactions/2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0"
                      },
                      "receive_tx": {
                        "chain_id": "noble-1",
                        "tx_hash": "D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4",
                        "explorer_link": "https://www.mintscan.io/noble/txs/D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4"
                      },
                      "acknowledge_tx": {
                        "chain_id": "osmosis-1",
                        "tx_hash": "D6EB805D774E89F81FBBBAF4DDE5FE22EF5237B89AD8178080C024237443783E",
                        "explorer_link": "https://www.mintscan.io/osmosis/transactions/D6EB805D774E89F81FBBBAF4DDE5FE22EF5237B89AD8178080C024237443783E"
                      },
                      "timeout_tx": null,
                      "error": null
                    },
                    "src_chain_id": "osmosis-1",
                    "dst_chain_id": "noble-1"
                  }
                },
                {
                  "ibc_transfer": {
                    "from_chain_id": "noble-1",
                    "to_chain_id": "dydx-mainnet-1",
                    "state": "TRANSFER_SUCCESS",
                    "packet_txs": {
                      "send_tx": {
                        "chain_id": "noble-1",
                        "tx_hash": "D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4",
                        "explorer_link": "https://www.mintscan.io/noble/txs/D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4"
                      },
                      "receive_tx": {
                        "chain_id": "dydx-mainnet-1",
                        "tx_hash": "1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579",
                        "explorer_link": "https://www.mintscan.io/dydx/txs/1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579"
                      },
                      "acknowledge_tx": {
                        "chain_id": "noble-1",
                        "tx_hash": "64155D5128022E445302CDABBF962707717D7E9D8FCCCE895DAD39D787763768",
                        "explorer_link": "https://www.mintscan.io/noble/txs/64155D5128022E445302CDABBF962707717D7E9D8FCCCE895DAD39D787763768"
                      },
                      "timeout_tx": null,
                      "error": null
                    },
                    "src_chain_id": "noble-1",
                    "dst_chain_id": "dydx-mainnet-1"
                  }
                }
              ],
              "next_blocking_transfer": null,
              "transfer_asset_release": {
                "chain_id": "dydx-mainnet-1",
                "denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "released": true
              },
              "error": null
            }
          ],
          "state": "STATE_COMPLETED_SUCCESS",
          "transfer_sequence": [
            {
              "axelar_transfer": {
                "from_chain_id": "1",
                "to_chain_id": "osmosis-1",
                "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                "state": "AXELAR_TRANSFER_SUCCESS",
                "txs": {
                  "contract_call_with_token_txs": {
                    "send_tx": {
                      "chain_id": "1",
                      "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                      "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                    },
                    "gas_paid_tx": {
                      "chain_id": "1",
                      "tx_hash": "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                      "explorer_link": "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c"
                    },
                    "confirm_tx": {
                      "chain_id": "axelar-dojo-1",
                      "tx_hash": "282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0",
                      "explorer_link": "https://mintscan.io/axelar/tx/282FEBD83D4D67558D40D94D2B1F212738CC293E7426123D3A415E134E41C0E0"
                    },
                    "approve_tx": null,
                    "execute_tx": {
                      "chain_id": "osmosis-1",
                      "tx_hash": "2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0",
                      "explorer_link": "https://www.mintscan.io/osmosis/transactions/2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0"
                    },
                    "error": null
                  }
                },
                "axelar_scan_link": "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                "src_chain_id": "1",
                "dst_chain_id": "osmosis-1"
              }
            },
            {
              "ibc_transfer": {
                "from_chain_id": "osmosis-1",
                "to_chain_id": "noble-1",
                "state": "TRANSFER_SUCCESS",
                "packet_txs": {
                  "send_tx": {
                    "chain_id": "osmosis-1",
                    "tx_hash": "2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0",
                    "explorer_link": "https://www.mintscan.io/osmosis/transactions/2ED2F33533EBE597EEA656F7822F2CE585240544BC3E78F6945E6446997740F0"
                  },
                  "receive_tx": {
                    "chain_id": "noble-1",
                    "tx_hash": "D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4",
                    "explorer_link": "https://www.mintscan.io/noble/txs/D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4"
                  },
                  "acknowledge_tx": {
                    "chain_id": "osmosis-1",
                    "tx_hash": "D6EB805D774E89F81FBBBAF4DDE5FE22EF5237B89AD8178080C024237443783E",
                    "explorer_link": "https://www.mintscan.io/osmosis/transactions/D6EB805D774E89F81FBBBAF4DDE5FE22EF5237B89AD8178080C024237443783E"
                  },
                  "timeout_tx": null,
                  "error": null
                },
                "src_chain_id": "osmosis-1",
                "dst_chain_id": "noble-1"
              }
            },
            {
              "ibc_transfer": {
                "from_chain_id": "noble-1",
                "to_chain_id": "dydx-mainnet-1",
                "state": "TRANSFER_SUCCESS",
                "packet_txs": {
                  "send_tx": {
                    "chain_id": "noble-1",
                    "tx_hash": "D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4",
                    "explorer_link": "https://www.mintscan.io/noble/txs/D8E14E9BD6E1FB14BC1451E4F973A070E35257FF7D392D9F499BAD0CA9ECAFD4"
                  },
                  "receive_tx": {
                    "chain_id": "dydx-mainnet-1",
                    "tx_hash": "1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579",
                    "explorer_link": "https://www.mintscan.io/dydx/txs/1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579"
                  },
                  "acknowledge_tx": {
                    "chain_id": "noble-1",
                    "tx_hash": "64155D5128022E445302CDABBF962707717D7E9D8FCCCE895DAD39D787763768",
                    "explorer_link": "https://www.mintscan.io/noble/txs/64155D5128022E445302CDABBF962707717D7E9D8FCCCE895DAD39D787763768"
                  },
                  "timeout_tx": null,
                  "error": null
                },
                "src_chain_id": "noble-1",
                "dst_chain_id": "dydx-mainnet-1"
              }
            }
          ],
          "next_blocking_transfer": null,
          "transfer_asset_release": {
            "chain_id": "dydx-mainnet-1",
            "denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
            "released": true
          },
          "error": null,
          "status": "STATE_COMPLETED"
        }
    """
    internal val withdrawToBnbBSCSuccess =
        """{
    "transfers": [
      {
        "state": "STATE_COMPLETED_SUCCESS",
        "transfer_sequence": [
          {
            "ibc_transfer": {
              "from_chain_id": "dydx-mainnet-1",
              "to_chain_id": "noble-1",
              "state": "TRANSFER_SUCCESS",
              "packet_txs": {
                "send_tx": {
                  "chain_id": "dydx-mainnet-1",
                  "tx_hash": "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                  "explorer_link": "https://www.mintscan.io/dydx/txs/7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A"
                },
                "receive_tx": {
                  "chain_id": "noble-1",
                  "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
                  "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
                },
                "acknowledge_tx": {
                  "chain_id": "dydx-mainnet-1",
                  "tx_hash": "9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89",
                  "explorer_link": "https://www.mintscan.io/dydx/txs/9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89"
                },
                "timeout_tx": null,
                "error": null
              },
              "src_chain_id": "dydx-mainnet-1",
              "dst_chain_id": "noble-1"
            }
          },
          {
            "ibc_transfer": {
              "from_chain_id": "noble-1",
              "to_chain_id": "osmosis-1",
              "state": "TRANSFER_SUCCESS",
              "packet_txs": {
                "send_tx": {
                  "chain_id": "noble-1",
                  "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
                  "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
                },
                "receive_tx": {
                  "chain_id": "osmosis-1",
                  "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                  "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                },
                "acknowledge_tx": {
                  "chain_id": "noble-1",
                  "tx_hash": "21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9",
                  "explorer_link": "https://www.mintscan.io/noble/txs/21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9"
                },
                "timeout_tx": null,
                "error": null
              },
              "src_chain_id": "noble-1",
              "dst_chain_id": "osmosis-1"
            }
          },
          {
            "axelar_transfer": {
              "from_chain_id": "osmosis-1",
              "to_chain_id": "56",
              "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
              "state": "AXELAR_TRANSFER_SUCCESS",
              "txs": {
                "contract_call_with_token_txs": {
                  "send_tx": {
                    "chain_id": "osmosis-1",
                    "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                    "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                  },
                  "gas_paid_tx": {
                    "chain_id": "osmosis-1",
                    "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                    "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                  },
                  "confirm_tx": {
                    "chain_id": "axelar-dojo-1",
                    "tx_hash": "8993C64807AA17F5C8D0B77B25D26CAE9CB4742EDB985793AF4A06AC61143FA9",
                    "explorer_link": "https://mintscan.io/axelar/tx/8993C64807AA17F5C8D0B77B25D26CAE9CB4742EDB985793AF4A06AC61143FA9"
                  },
                  "approve_tx": {
                    "chain_id": "56",
                    "tx_hash": "0x0af1a1868e85b8551713efb26f401ccd395fb9f1e5f493ee7af9dc5f01993a55",
                    "explorer_link": "https://bscscan.com/tx/0x0af1a1868e85b8551713efb26f401ccd395fb9f1e5f493ee7af9dc5f01993a55"
                  },
                  "execute_tx": {
                    "chain_id": "56",
                    "tx_hash": "0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb",
                    "explorer_link": "https://bscscan.com/tx/0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb"
                  },
                  "error": null
                }
              },
              "axelar_scan_link": "https://axelarscan.io/gmp/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
              "src_chain_id": "osmosis-1",
              "dst_chain_id": "56"
            }
          }
        ],
        "next_blocking_transfer": null,
        "transfer_asset_release": {
          "chain_id": "56",
          "denom": "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
          "released": true
        },
        "error": null
      }
    ],
    "state": "STATE_COMPLETED_SUCCESS",
    "transfer_sequence": [
      {
        "ibc_transfer": {
          "from_chain_id": "dydx-mainnet-1",
          "to_chain_id": "noble-1",
          "state": "TRANSFER_SUCCESS",
          "packet_txs": {
            "send_tx": {
              "chain_id": "dydx-mainnet-1",
              "tx_hash": "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
              "explorer_link": "https://www.mintscan.io/dydx/txs/7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A"
            },
            "receive_tx": {
              "chain_id": "noble-1",
              "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
              "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
            },
            "acknowledge_tx": {
              "chain_id": "dydx-mainnet-1",
              "tx_hash": "9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89",
              "explorer_link": "https://www.mintscan.io/dydx/txs/9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89"
            },
            "timeout_tx": null,
            "error": null
          },
          "src_chain_id": "dydx-mainnet-1",
          "dst_chain_id": "noble-1"
        }
      },
      {
        "ibc_transfer": {
          "from_chain_id": "noble-1",
          "to_chain_id": "osmosis-1",
          "state": "TRANSFER_SUCCESS",
          "packet_txs": {
            "send_tx": {
              "chain_id": "noble-1",
              "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
              "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
            },
            "receive_tx": {
              "chain_id": "osmosis-1",
              "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
              "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
            },
            "acknowledge_tx": {
              "chain_id": "noble-1",
              "tx_hash": "21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9",
              "explorer_link": "https://www.mintscan.io/noble/txs/21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9"
            },
            "timeout_tx": null,
            "error": null
          },
          "src_chain_id": "noble-1",
          "dst_chain_id": "osmosis-1"
        }
      },
      {
        "axelar_transfer": {
          "from_chain_id": "osmosis-1",
          "to_chain_id": "56",
          "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
          "state": "AXELAR_TRANSFER_SUCCESS",
          "txs": {
            "contract_call_with_token_txs": {
              "send_tx": {
                "chain_id": "osmosis-1",
                "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
              },
              "gas_paid_tx": {
                "chain_id": "osmosis-1",
                "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
              },
              "confirm_tx": {
                "chain_id": "axelar-dojo-1",
                "tx_hash": "8993C64807AA17F5C8D0B77B25D26CAE9CB4742EDB985793AF4A06AC61143FA9",
                "explorer_link": "https://mintscan.io/axelar/tx/8993C64807AA17F5C8D0B77B25D26CAE9CB4742EDB985793AF4A06AC61143FA9"
              },
              "approve_tx": {
                "chain_id": "56",
                "tx_hash": "0x0af1a1868e85b8551713efb26f401ccd395fb9f1e5f493ee7af9dc5f01993a55",
                "explorer_link": "https://bscscan.com/tx/0x0af1a1868e85b8551713efb26f401ccd395fb9f1e5f493ee7af9dc5f01993a55"
              },
              "execute_tx": {
                "chain_id": "56",
                "tx_hash": "0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb",
                "explorer_link": "https://bscscan.com/tx/0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb"
              },
              "error": null
            }
          },
          "axelar_scan_link": "https://axelarscan.io/gmp/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
          "src_chain_id": "osmosis-1",
          "dst_chain_id": "56"
        }
      }
    ],
    "next_blocking_transfer": null,
    "transfer_asset_release": {
      "chain_id": "56",
      "denom": "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
      "released": true
    },
    "error": null,
    "status": "STATE_COMPLETED"
  }"""
    internal val withdrawToBnbBSCSubmitted = """{
        "transfers": [
        {
            "state": "STATE_SUBMITTED",
            "transfer_sequence": [],
            "next_blocking_transfer": null,
            "transfer_asset_release": null,
            "error": null
        }
        ],
        "state": "STATE_SUBMITTED",
        "transfer_sequence": [],
        "next_blocking_transfer": null,
        "transfer_asset_release": null,
        "error": null,
        "status": "STATE_SUBMITTED"
    }"""

    internal val withdrawToBnbBSCPending = """
        {
            "transfers": [
                {
                    "state": "STATE_PENDING",
                    "transfer_sequence": [
                        {
                            "ibc_transfer": {
                                "from_chain_id": "dydx-mainnet-1",
                                "to_chain_id": "noble-1",
                                "state": "TRANSFER_SUCCESS",
                                "packet_txs": {
                                    "send_tx": {
                                        "chain_id": "dydx-mainnet-1",
                                        "tx_hash": "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                                        "explorer_link": "https://www.mintscan.io/dydx/txs/7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A"
                                    },
                                    "receive_tx": {
                                        "chain_id": "noble-1",
                                        "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
                                        "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
                                    },
                                    "acknowledge_tx": {
                                        "chain_id": "dydx-mainnet-1",
                                        "tx_hash": "9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89",
                                        "explorer_link": "https://www.mintscan.io/dydx/txs/9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89"
                                    },
                                    "timeout_tx": null,
                                    "error": null
                                },
                                "src_chain_id": "dydx-mainnet-1",
                                "dst_chain_id": "noble-1"
                            }
                        },
                        {
                            "ibc_transfer": {
                                "from_chain_id": "noble-1",
                                "to_chain_id": "osmosis-1",
                                "state": "TRANSFER_SUCCESS",
                                "packet_txs": {
                                    "send_tx": {
                                        "chain_id": "noble-1",
                                        "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
                                        "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
                                    },
                                    "receive_tx": {
                                        "chain_id": "osmosis-1",
                                        "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                        "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                                    },
                                    "acknowledge_tx": {
                                        "chain_id": "noble-1",
                                        "tx_hash": "21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9",
                                        "explorer_link": "https://www.mintscan.io/noble/txs/21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9"
                                    },
                                    "timeout_tx": null,
                                    "error": null
                                },
                                "src_chain_id": "noble-1",
                                "dst_chain_id": "osmosis-1"
                            }
                        },
                        {
                            "axelar_transfer": {
                                "from_chain_id": "osmosis-1",
                                "to_chain_id": "56",
                                "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                                "state": "AXELAR_TRANSFER_PENDING_CONFIRMATION",
                                "txs": {
                                    "contract_call_with_token_txs": {
                                        "send_tx": {
                                            "chain_id": "osmosis-1",
                                            "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                            "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                                        },
                                        "gas_paid_tx": {
                                            "chain_id": "osmosis-1",
                                            "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                            "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                                        },
                                        "confirm_tx": null,
                                        "approve_tx": null,
                                        "execute_tx": null,
                                        "error": null
                                    }
                                },
                                "axelar_scan_link": "https://axelarscan.io/gmp/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                "src_chain_id": "osmosis-1",
                                "dst_chain_id": "56"
                            }
                        }
                    ],
                    "next_blocking_transfer": {
                        "transfer_sequence_index": 2
                    },
                    "transfer_asset_release": null,
                    "error": null
                }
            ],
            "state": "STATE_PENDING",
            "transfer_sequence": [
                {
                    "ibc_transfer": {
                        "from_chain_id": "dydx-mainnet-1",
                        "to_chain_id": "noble-1",
                        "state": "TRANSFER_SUCCESS",
                        "packet_txs": {
                            "send_tx": {
                                "chain_id": "dydx-mainnet-1",
                                "tx_hash": "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                                "explorer_link": "https://www.mintscan.io/dydx/txs/7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A"
                            },
                            "receive_tx": {
                                "chain_id": "noble-1",
                                "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
                                "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
                            },
                            "acknowledge_tx": {
                                "chain_id": "dydx-mainnet-1",
                                "tx_hash": "9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89",
                                "explorer_link": "https://www.mintscan.io/dydx/txs/9CAE6355C37CE8DF48DFDAC86D538490170B10D4EF15ACF1CB281D4D4453DB89"
                            },
                            "timeout_tx": null,
                            "error": null
                        },
                        "src_chain_id": "dydx-mainnet-1",
                        "dst_chain_id": "noble-1"
                    }
                },
                {
                    "ibc_transfer": {
                        "from_chain_id": "noble-1",
                        "to_chain_id": "osmosis-1",
                        "state": "TRANSFER_SUCCESS",
                        "packet_txs": {
                            "send_tx": {
                                "chain_id": "noble-1",
                                "tx_hash": "FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2",
                                "explorer_link": "https://www.mintscan.io/noble/txs/FB1D78E9FEE7143ED78075DF1EA04A1B69560CB71010577B542E9AA6622833A2"
                            },
                            "receive_tx": {
                                "chain_id": "osmosis-1",
                                "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                            },
                            "acknowledge_tx": {
                                "chain_id": "noble-1",
                                "tx_hash": "21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9",
                                "explorer_link": "https://www.mintscan.io/noble/txs/21E98EF6CD55BFB73F5585F70F15F90DB0F9CBC3B56D75E692BA488B7701E5A9"
                            },
                            "timeout_tx": null,
                            "error": null
                        },
                        "src_chain_id": "noble-1",
                        "dst_chain_id": "osmosis-1"
                    }
                },
                {
                    "axelar_transfer": {
                        "from_chain_id": "osmosis-1",
                        "to_chain_id": "56",
                        "type": "AXELAR_TRANSFER_CONTRACT_CALL_WITH_TOKEN",
                        "state": "AXELAR_TRANSFER_PENDING_CONFIRMATION",
                        "txs": {
                            "contract_call_with_token_txs": {
                                "send_tx": {
                                    "chain_id": "osmosis-1",
                                    "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                    "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                                },
                                "gas_paid_tx": {
                                    "chain_id": "osmosis-1",
                                    "tx_hash": "169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                                    "explorer_link": "https://www.mintscan.io/osmosis/transactions/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974"
                                },
                                "confirm_tx": null,
                                "approve_tx": null,
                                "execute_tx": null,
                                "error": null
                            }
                        },
                        "axelar_scan_link": "https://axelarscan.io/gmp/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                        "src_chain_id": "osmosis-1",
                        "dst_chain_id": "56"
                    }
                }
            ],
            "next_blocking_transfer": {
                "transfer_sequence_index": 2
            },
            "transfer_asset_release": null,
            "error": null,
            "status": "STATE_PENDING"
        }
    """
    internal val depositFromUSDCEthPending = """{
    "transfers": [
        {
            "state": "STATE_PENDING",
            "transfer_sequence": [
                {
                    "cctp_transfer": {
                        "from_chain_id": "1",
                        "to_chain_id": "noble-1",
                        "state": "CCTP_TRANSFER_PENDING_CONFIRMATION",
                        "txs": {
                            "send_tx": {
                                "chain_id": "1",
                                "tx_hash": "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                                "explorer_link": "https://etherscan.io/tx/0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D"
                            },
                            "receive_tx": null
                        },
                        "src_chain_id": "1",
                        "dst_chain_id": "noble-1"
                    }
                }
            ],
            "next_blocking_transfer": {
                "transfer_sequence_index": 0
            },
            "transfer_asset_release": null,
            "error": null
        }
    ],
    "state": "STATE_PENDING",
    "transfer_sequence": [
        {
            "cctp_transfer": {
                "from_chain_id": "1",
                "to_chain_id": "noble-1",
                "state": "CCTP_TRANSFER_PENDING_CONFIRMATION",
                "txs": {
                    "send_tx": {
                        "chain_id": "1",
                        "tx_hash": "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                        "explorer_link": "https://etherscan.io/tx/0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D"
                    },
                    "receive_tx": null
                },
                "src_chain_id": "1",
                "dst_chain_id": "noble-1"
            }
        }
    ],
    "next_blocking_transfer": {
        "transfer_sequence_index": 0
    },
    "transfer_asset_release": null,
    "error": null,
    "status": "STATE_PENDING"
}"""
    internal val depositFromUSDCEthSuccess = """{
    "transfers": [
        {
            "state": "STATE_COMPLETED_SUCCESS",
            "transfer_sequence": [
                {
                    "cctp_transfer": {
                        "from_chain_id": "1",
                        "to_chain_id": "noble-1",
                        "state": "CCTP_TRANSFER_RECEIVED",
                        "txs": {
                            "send_tx": {
                                "chain_id": "1",
                                "tx_hash": "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                                "explorer_link": "https://etherscan.io/tx/0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D"
                            },
                            "receive_tx": {
                                "chain_id": "noble-1",
                                "tx_hash": "A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12",
                                "explorer_link": "https://www.mintscan.io/noble/txs/A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12"
                            }
                        },
                        "src_chain_id": "1",
                        "dst_chain_id": "noble-1"
                    }
                }
            ],
            "next_blocking_transfer": null,
            "transfer_asset_release": {
                "chain_id": "noble-1",
                "denom": "uusdc",
                "released": true
            },
            "error": null
        }
    ],
    "state": "STATE_COMPLETED_SUCCESS",
    "transfer_sequence": [
        {
            "cctp_transfer": {
                "from_chain_id": "1",
                "to_chain_id": "noble-1",
                "state": "CCTP_TRANSFER_RECEIVED",
                "txs": {
                    "send_tx": {
                        "chain_id": "1",
                        "tx_hash": "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                        "explorer_link": "https://etherscan.io/tx/0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D"
                    },
                    "receive_tx": {
                        "chain_id": "noble-1",
                        "tx_hash": "A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12",
                        "explorer_link": "https://www.mintscan.io/noble/txs/A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12"
                    }
                },
                "src_chain_id": "1",
                "dst_chain_id": "noble-1"
            }
        }
    ],
    "next_blocking_transfer": null,
    "transfer_asset_release": {
        "chain_id": "noble-1",
        "denom": "uusdc",
        "released": true
    },
    "error": null,
    "status": "STATE_COMPLETED"
}"""

    internal val withdrawToUSDCEthPending = """{
  "transfers": [
      {
          "state": "STATE_PENDING",
          "transfer_sequence": [
              {
                  "cctp_transfer": {
                      "from_chain_id": "noble-1",
                      "to_chain_id": "1",
                      "state": "CCTP_TRANSFER_SENT",
                      "txs": {
                          "send_tx": {
                              "chain_id": "noble-1",
                              "tx_hash": "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                              "explorer_link": "https://www.mintscan.io/noble/txs/E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25"
                          },
                          "receive_tx": null
                      },
                      "src_chain_id": "noble-1",
                      "dst_chain_id": "1"
                  }
              }
          ],
          "next_blocking_transfer": {
              "transfer_sequence_index": 0
          },
          "transfer_asset_release": null,
          "error": null
      }
  ],
  "state": "STATE_PENDING",
  "transfer_sequence": [
      {
          "cctp_transfer": {
              "from_chain_id": "noble-1",
              "to_chain_id": "1",
              "state": "CCTP_TRANSFER_SENT",
              "txs": {
                  "send_tx": {
                      "chain_id": "noble-1",
                      "tx_hash": "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                      "explorer_link": "https://www.mintscan.io/noble/txs/E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25"
                  },
                  "receive_tx": null
              },
              "src_chain_id": "noble-1",
              "dst_chain_id": "1"
          }
      }
  ],
  "next_blocking_transfer": {
      "transfer_sequence_index": 0
  },
  "transfer_asset_release": null,
  "error": null,
  "status": "STATE_PENDING"
}"""

    internal val withdrawToUSDCEthSuccess = """{
  "transfers": [
    {
      "state": "STATE_COMPLETED_SUCCESS",
      "transfer_sequence": [
        {
          "cctp_transfer": {
            "from_chain_id": "noble-1",
            "to_chain_id": "1",
            "state": "CCTP_TRANSFER_RECEIVED",
            "txs": {
              "send_tx": {
                "chain_id": "noble-1",
                "tx_hash": "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                "explorer_link": "https://www.mintscan.io/noble/txs/E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25"
              },
              "receive_tx": {
                "chain_id": "1",
                "tx_hash": "0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37",
                "explorer_link": "https://etherscan.io/tx/0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37"
              }
            },
            "src_chain_id": "noble-1",
            "dst_chain_id": "1"
          }
        }
      ],
      "next_blocking_transfer": null,
      "transfer_asset_release": {
        "chain_id": "1",
        "denom": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
        "released": true
      },
      "error": null
    }
  ],
  "state": "STATE_COMPLETED_SUCCESS",
  "transfer_sequence": [
    {
      "cctp_transfer": {
        "from_chain_id": "noble-1",
        "to_chain_id": "1",
        "state": "CCTP_TRANSFER_RECEIVED",
        "txs": {
          "send_tx": {
            "chain_id": "noble-1",
            "tx_hash": "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
            "explorer_link": "https://www.mintscan.io/noble/txs/E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25"
          },
          "receive_tx": {
            "chain_id": "1",
            "tx_hash": "0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37",
            "explorer_link": "https://etherscan.io/tx/0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37"
          }
        },
        "src_chain_id": "noble-1",
        "dst_chain_id": "1"
      }
    }
  ],
  "next_blocking_transfer": null,
  "transfer_asset_release": {
    "chain_id": "1",
    "denom": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
    "released": true
  },
  "error": null,
  "status": "STATE_COMPLETED"
}"""
}
