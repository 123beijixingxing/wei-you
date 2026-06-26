# 付款申请 api

## 1.修订记录

| 版本 | 日期       | 修订人 | 说明 |
| ---- | ---------- | ------ | ---- |
| V1.0 | 2025-09-01 | 侯彬彬 | 初稿 |

## 2.说明

## 3.接口

### 3.1 付款申请

#### 3.1.1 查询付款申请列表

- **Method**: GET

- **Uri**: /paymentApply/list

- **Param**: 

  | key             | value | 说明       | 是否必填 | 备注           |
  | --------------- | ----- | ---------- | -------- | -------------- |
  | page            | int   |            | Y        |                |
  | limit           | int   |            | Y        |                |
  | topic           |       |            |          |                |
  | cCode           |       |            |          |                |
  | startDate       |       | 申请日期始 |          |                |
  | endDate         |       | 申请日期止 |          |                |
  | statuses        |       | 申请状态   |          | 多选，逗号分隔 |
  | invoiceStatuses |       | 发票状态   |          | 多选，逗号分隔 |
  |                 |       |            |          |                |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "total":"2",
          "list":[{
              "paymentId":"233",//paymentId
              "topic":"string",//主题
              "cCode":"string",//付款申请单号
              "dDate":"string",//申请日期
              "status":"string",//申请状态
              "statusName":"string",//申请状态
              "payMethod":"string",//付款方式
              "payMethodName":"string",//付款方式
              "invoiceStatus":"string",//发票状态
              "invoiceStatusName":"string",//发票状态
              "amount":"string",//申请金额
              "personName":"string",//申请人
              "deptName":"string",//所属部门
              "remark":"string",//备注
          }]
          }
  }
  ```

#### 3.1.2 新增付款申请

- **Method**: POST

- **Uri**: /paymentApply

- **Param**: 

  | key     | value | 说明     | 是否必填 | 备注       |
  | ------- | ----- | -------- | -------- | ---------- |
  | preview | bool  | 预览     | N        | true/false |
  | commit  | bool  | 审批提交 | N        | true/false |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
     
     "topic":"string",//主题
     
     "payMethod":"string",//付款方式
      "currentPaystep":"string",//本次付款阶段
     
     "amount":"string",//申请金额
      "currentStepAmount":"string",//本次申请金额
      
     "personId":"string",//申请人
     "personName":"string",//申请人
     "deptId":"string",//所属部门
      "deptName":"string",//所属部门
      
              "remark":"string",//备注
      "files":[{}]
              "recipientInfo":"string",//收款信息
      "paymentPlans":[{
          "paystep":"string",//阶段
              "payRatio":"string",//比例
              
              "unpay":"string",//未付金额(付款金额)  
      }]
              
      "expense":[{
      	"costTypeEnumId":"string",//费用类型
              "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseApplyAmount":"string",//申请金额
              "startTime":"string",//时间起?
              "endTime":"string",//时间止?
              "invoiceIds":["string"],//发票ids，todo：识别id
              "share":[{
              	"deptId":"string",//报销部门
              	"deptName":"string",//报销部门
              	"ratio":"string",//分摊比例
              	"amount":"string",//分摊金额
  			}],//
              
  	}]
              
  }
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.1.3 修改付款申请

- **Method**: PUT

- **Uri**: /paymentApply

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注 |
  | --------- | ------ | ---- | -------- | ---- |
  | paymentId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
     
     "topic":"string",//主题
     
     "payMethod":"string",//付款方式
      "currentPaystep":"string",//本次付款阶段
     
     "amount":"string",//申请金额
      "currentStepAmount":"string",//本次申请金额
      
     "personId":"string",//申请人
     "personName":"string",//申请人
     "deptId":"string",//所属部门
      "deptName":"string",//所属部门
      
              "remark":"string",//备注
              "recipientInfo":"string",//收款信息
      "paymentPlans":[{
          "paystep":"string",//阶段
              "payRatio":"string",//比例
              "paid":"string",//已付金额
              "unpay":"string",//未付金额  
      }]
              
      "expense":[{
      	"costTypeEnumId":"string",//费用类型
              "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseApplyAmount":"string",//申请金额
              "startTime":"string",//时间起
              "endTime":"string",//时间止
              "invoiceIds":["string"],//发票ids
              "share":[{
              	"deptId":"string",//报销部门
              	"deptName":"string",//报销部门
              	"ratio":"string",//分摊比例
              	"amount":"string",//分摊金额
  			}],//
              
  	}]
              
  }
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.1.3 阶段付款修改

- **Method**: PUT

- **Uri**: /paymentApply/planUpdate

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注 |
  | --------- | ------ | ---- | -------- | ---- |
  | paymentId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
   "currentPaystep":"string",//本次付款阶段
   "currentStepAmount":"string",//本次申请金额     
   "currentPaystepId":"string",//本次付款阶段planId
  }
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//本次付款阶段planId
      }
  }
  ```

#### 3.1.3 单条明细修改(待开发票)

- **Method**: PUT

- **Uri**: /paymentApply/detailUpdate

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注 |
  | --------- | ------ | ---- | -------- | ---- |
  | paymentId | string |      | Y        |      |
  | expenseId |        |      |          |      |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
   //todo    
  }
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 

#### 3.1.4 查询付款申请详情

- **Method**: GET

- **Uri**: /paymentApply

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注 |
  | --------- | ------ | ---- | -------- | ---- |
  | paymentId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "enumId":"1111000000000011001",
          "enumType":"1111000000000011001",
          "paymentId":"string",//paymentId
     
     		"topic":"string",//主题
     		"cCode":"string",//付款申请单号
           "dDate":"string",//申请日期
     		"payMethod":"string",//付款方式 onetime 一次,bystages 多次
     		 "currentPaystep":"string",//本次付款阶段
     		
     		"amount":"string",//申请金额
     		 "currentStepAmount":"string",//本次申请金额
     		 
     		"personId":"string",//申请人
     		"personName":"string",//申请人
     		"deptId":"string",//所属部门
     		 "deptName":"string",//所属部门
     		 
     		         "remark":"string",//备注
          "createUser":"string",//制单人
          "status"："string",//状态
          "statusName"："string",//状态 notCommit 未提交,inApproval 审核中,unPaid 未支付,partPaid 部分支付,allPaid 已支付,
     		         				"recipientInfo":"string",//收款信息
     		 "paymentPlans":[{
     		     "paystep":"string",//阶段
     		         "payRatio":"string",//比例
     		         "paid":"string",//已付金额
     		         "unpay":"string",//未付金额  
          "status"："string",//状态
     		 }],//付款计划
      "paymentPlanContent":"string",//预付款50%，尾款50%，共计15,000.00元。(付款计划的完整拼接内容)
     		 "files":[{}]//附件？
      	"approvalStatus":"",//
      	"approvalStatusName":"",//
      	"approvalTime":"",//
      	"approvalId":"",//
      	
     		 "expense":[{//费用明细list
      			"costTypeEnumId":"string",//费用类型
      			"costTypeNameJoin":"string",//费用类型
      			"expenseInvoiceStatus":"string",//发票状态
      			"expenseInvoiceAmount":"string",//发票金额
      			"expenseApplyAmount":"string",//申请金额
      			"startTime":"string",//时间起
      			"endTime":"string",//时间止
      			"invoiceIds":[{}],//发票ids
      			"invoiceStatistics":[{
          				"invoiceType":"string",//类型
          				"invoiceTypeName":"string",//类型
          		    	"count":"string",//数量
          				"amount":"string",//金额
      				}],//发票统计信息
      			"share":[{
      				"deptId":"string",//报销部门
      				"deptName":"string",//报销部门
      				"ratio":"string",//分摊比例
      				"amount":"string",//分摊金额
      			}],//
      			
  				}]
              
  	}
  }
  ```

#### 3.1.4_1 查询付款申请明细详情（代开发票）

- **Method**: GET

- **Uri**: /paymentApply/expense

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注 |
  | --------- | ------ | ---- | -------- | ---- |
  | paymentId | string |      | Y        |      |
  | expenseId |        |      |          |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      			"costTypeEnumId":"string",//费用类型
      			"costTypeNameJoin":"string",//费用类型
          		    "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseApplyAmount":"string",//申请金额
          		    "startTime":"string",//时间起
          		    "endTime":"string",//时间止
          		    "invoiceIds":["string"],//发票ids
          			"invoiceStatistics":[{
          				"invoiceType":"string",//类型
          				"invoiceTypeName":"string",//类型
          		    	"count":"string",//数量
          				"amount":"string",//金额
      				}],//发票统计信息
      				"invoiceDetails":[{
                          "invoiceId":"string",//发票id
          				"invoiceType":"string",//类型
          				"invoiceTypeName":"string",//类型
          		    	"no":"string",//发票号？
          				"amount":"string",//金额
      				}],//发票具体信息
          		    "share":[{
          		    	"deptId":"string",//报销部门
          		    	"deptName":"string",//报销部门
          		    	"ratio":"string",//分摊比例
          		    	"amount":"string",//分摊金额
  					}],//
              
  				}
  ```

#### 



#### 3.1.5 删除付款申请

- **Method**: DELETE

- **Uri**: /paymentApply

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注 |
  | --------- | ------ | ---- | -------- | ---- |
  | paymentId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.1.6 导出付款申请

- **Method**: GET

- **Uri**: /paymentApply/export

- **Param**: 

  | key       | value  | 说明 | 是否必填 | 备注     |
  | --------- | ------ | ---- | -------- | -------- |
  | paymentId | string |      | Y        |          |
  | type      | string |      | Y        | pdf,word |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.1.7 查询待开发票列表(付款申请明细)

- **Method**: GET

- **Uri**: /paymentApply/unbilled/list

- **Param**: 

  | key   | value | 说明 | 是否必填 | 备注 |
  | ----- | ----- | ---- | -------- | ---- |
  | page  | int   |      | Y        |      |
  | limit | int   |      | Y        |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "total":"2",
          "list":[{
              "expenseId":"string",//明细id
              "paymentId":"233",//paymentId
              "topic":"string",//主题
              "cCode":"string",//付款申请单号
              "dDate":"string",//申请日期
              
              "recordType":"string",//单据类型
              "costTypeEnumId":"string",//费用类型
              "reupApprovalStatus":"string",//补票审批状态
  
              "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseApplyAmount":"string",//申请金额
              "personName":"string",//申请人
              "deptName":"string",//所属部门
              "remark":"string",//备注
              
              
          }]
          }
  }
  ```

#### 

### 3.2 报销管理

#### 3.2.1 查询报销单列表

- **Method**: GET

- **Uri**: /reim/list

- **Param**: 

  | key                | value | 说明     | 是否必填 | 备注                 |
  | ------------------ | ----- | -------- | -------- | -------------------- |
  | page               | int   |          | Y        |                      |
  | limit              | int   |          | Y        |                      |
  | topic              |       |          |          |                      |
  | cCode              |       |          |          |                      |
  | startDate          |       |          |          |                      |
  | endDate            |       |          |          |                      |
  | personName         |       |          |          |                      |
  | reimTypeEnumId     |       |          |          |                      |
  | status             |       | 报销状态 |          |                      |
  | statuses           |       | 报销状态 |          | 多选，逗号分隔       |
  | deptName           |       | 部门     |          |                      |
  | invoiceStatus      |       | 发票状态 |          |                      |
  |                    |       |          |          |                      |
  | statusList         |       | 报销状态 |          | post参数传[“string”] |
  | invoiceStatusList  |       | 发票状态 |          | post参数传[“string”] |
  | reimTypeEnumIdList |       | 报销类型 |          | post参数传[“string”] |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "total":"2",
          "list":[{
              "reimId":"string",//reimId
              "reimTypeEnumId":"string",//报销类型
              "reimTypeEnumName":"string",//报销类型？
              "topic":"string",//主题
              "cCode":"string",//报销单号
              "dDate":"string",//报销日期
              "status":"string",//报销状态
              "statusName":"string",//报销状态
              "amount":"string",//报销金额
              "personName":"string",//报销人
              "deptName":"string",//所属部门
              "":"string",//
              "":"string",//
          }]
          }
  }
  ```

#### 3.2.2 新增报销单

- **Method**: POST

- **Uri**: /reim

- **Param**: 

  | key     | value | 说明 | 是否必填 | 备注       |
  | ------- | ----- | ---- | -------- | ---------- |
  | preview | bool  | 预览 | N        | true/false |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
     
     "topic":"string",//主题
     
     "amount":"string",//报销金额
      "settledAmount":"string",//核销金额
      "paidAmount":"string",//支付金额
      
     "personId":"string",//申请人
     "personName":"string",//申请人
     "deptId":"string",//所属部门
      "deptName":"string",//所属部门
      
     "remark":"string",//备注
      "workTravelApprovalId":["id"],
        
      "expense":[{
      	"costTypeEnumId":"string",//费用类型
              "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseReimAmount":"string",//报销金额
              "startTime":"string",//时间起
              "endTime":"string",//时间止
          "trafficFileId":"",//导入明细id
              "invoiceIds":["string"],//发票ids
          	"traffic":[{
              	"trafficDate":"string",//
              	"departure":"string",//出发地
              	"destination":"string",//目的地
              	"purpose":"string",//拜访目的
              	"transport":"string",//交通工具
              	"trafficCost":"string",//金额
          }] 
  	}],
      "borrowCheckIds":[{"borrowId"："",
                        "borrowAmount":"string"
                        }]//核销,用id和金额调用w接口
              
  }
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.2.3 修改报销单

- **Method**: PUT

- **Uri**: /reim

- **Param**: 

  | key    | value  | 说明 | 是否必填 | 备注 |
  | ------ | ------ | ---- | -------- | ---- |
  | reimId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
     
     "topic":"string",//主题
     
     "amount":"string",//报销金额
      "settledAmount":"string",//核销金额
      "paidAmount":"string",//支付金额
      
     "personId":"string",//申请人
     "personName":"string",//申请人
     "deptId":"string",//所属部门
      "deptName":"string",//所属部门
      
     "remark":"string",//备注
        
      "expense":[{
      	"costTypeEnumId":"string",//费用类型
              "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseReimAmount":"string",//报销金额
              "startTime":"string",//时间起
              "endTime":"string",//时间止
              "invoiceIds":["string"],//发票ids
          	"traffic":[{
              	"trafficDate":"string",//
              	"departure":"string",//出发地
              	"destination":"string",//目的地
              	"purpose":"string",//拜访目的
              	"transport":"string",//交通工具
              	"trafficCost":"string",//金额
          }]
              
  	}],
      "borrowCheckIds":["borrowId"]
              
  }
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.2.4 查询报销单详情

- **Method**: GET

- **Uri**: /reim

- **Param**: 

  | key    | value  | 说明 | 是否必填 | 备注 |
  | ------ | ------ | ---- | -------- | ---- |
  | reimId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "reimTypeEnumId":"string",//报销类型
          "reimType":"string",//报销类型
          "reimTypeEnumName":"string",//报销类型
          
    	"reimId":"string",//reimId
         "topic":"string",//主题
          "cCode":"string",//报销单号
              "dDate":"string",//报销日期
     "status":"string",//报销状态
          "statusName":"string",//报销状态
          "invoiceStatus":"string",//发票状态
          "invoiceStatusName":"string",//发票状态
          
         "amount":"string",//报销金额
          "settledAmount":"string",//核销金额
          "paidAmount":"string",//支付金额
  
         "personId":"string",//申请人
         "personName":"string",//申请人
         "deptId":"string",//所属部门
          "deptName":"string",//所属部门
  
          "createUser":"string",//制单人
         "remark":"string",//备注
          "files":[{}]//附件
      	"approvalStatus":"",//？
      	"approvalTime":"",//？
      	"approvalId":"",//？
          "approvalNotices":["市内交通费与住宿费合计金额超标！"]//单据的审批提醒
  
          "expense":[{//费用明细list
              "costTypeEnumId":"string",//费用类型
              "costType":"string",//费用类型
              "costTypeEnumName":"string",//费用类型
              "expenseInvoiceStatus":"string",//发票状态
              "expenseInvoiceStatusName":"string",//发票状态
              "expenseInvoiceAmount":"string",//发票金额
              "expenseReimAmount":"string",//报销金额
              "startTime":"string",//时间起
              "endTime":"string",//时间止
              "invoiceIds":[{}],//发票ids
              "invoiceStatistics":[{
          				"invoiceMediaType":"string",//类型
          				"invoiceMediaTypeName":"string",//类型
          		    	"count":"string",//数量
          				"amount":"string",//金额
      				}],//发票统计信息
              "traffic":[{
                  "trafficDate":"string",//
                  "departure":"string",//出发地
                  "destination":"string",//目的地
                  "purpose":"string",//拜访目的
                  "transport":"string",//交通工具
                  "trafficCost":"string",//金额
              }],
              "approvalNotices":["费用金额与发票金额不一致！"]//费用明细的审批提醒
  		}]
  
  	 }
  }
  ```

#### 3.2.5 删除报销单

- **Method**: DELETE

- **Uri**: /reim

- **Param**: 

  | key    | value  | 说明 | 是否必填 | 备注 |
  | ------ | ------ | ---- | -------- | ---- |
  | reimId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.2.6 导出报销单

- **Method**: GET

- **Uri**: /reim/export

- **Param**: 

  | key    | value  | 说明 | 是否必填 | 备注     |
  | ------ | ------ | ---- | -------- | -------- |
  | reimId | string |      | Y        |          |
  | type   | string |      | Y        | pdf,word |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "recordId":"string",//记录id
      }
  }
  ```

#### 3.2.7 获取交通明细导入数据

- **Method**: GET

- **Uri**: /reim/traffic/read

- **Param**: 

  | key    | value  | 说明 | 是否必填 | 备注 |
  | ------ | ------ | ---- | -------- | ---- |
  | fileId | string |      | Y        |      |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "list":[{
              	"trafficDate":"string",//
              	"departure":"string",//出发地
              	"destination":"string",//目的地
              	"purpose":"string",//拜访目的
              	"transport":"string",//交通工具
              	"trafficCost":"string",//金额
          }]
      }
  }
  ```

#### 3.2.8 费用是否超标

- **Method**: POST

- **Uri**: /reim/expense/isCostOver

- **Param**: 

  | key              | value  | 说明     | 是否必填 | 备注 |
  | ---------------- | ------ | -------- | -------- | ---- |
  | reimTypeEnumName | string | 报销类型 | Y        |      |
  | personId         | string | 报销人id | Y        |      |

- **Header**: NONE

- **RequestBody**: 

  ```json
  {
              "costType":"string",//费用类型
              "expenseReimAmount":"string",//报销金额
              "startTime":"string",//时间起
              "endTime":"string",//时间止
          
  	}
  ```

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "result": false,//true=超标，false=未超标
          "reason": "报销金额50元未超过市内交通费的标准100元"
      }
  }
  ```

### 3.3 凭证管理

#### 3.3.1 查询凭证列表

- **Method**: GET

- **Uri**: /voucher/list

- **Param**: 

  | key        | value  | 说明         | 是否必填 | 备注                                                         |
  | ---------- | ------ | ------------ | -------- | ------------------------------------------------------------ |
  | page       | int    |              | Y        |                                                              |
  | limit      | int    |              | Y        |                                                              |
  | moduleType |        | 单据模块名   | N        | fksq	付款申请；jkgl	借款管理；xgl	报销管理          |
  | recordType |        | 单据具体类型 | N        |                                                              |
  | status     | string | 凭证记录状态 |          | toBeGenerated 待生成<br />generatedFailed 生成失败<br />generated 已生成 |
  | topic      |        |              |          |                                                              |
  | recordCode |        |              |          |                                                              |
  | startDate  |        |              |          |                                                              |
  | endDate    |        |              |          |                                                              |
  | deptName   |        | 部门         |          |                                                              |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**: 

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true,
      "data": {
          "total":"2",
          "list":[{
                  "inoId": "1",//凭证号
                  "billDate": "2025-11-27",//2025-04-28
                  "moduleType": "fksq",//单据模块code
                  "moduleTypeName": "付款申请",//单据模块名
                  "recordType": "fksq_fksq",//单据类型
                  "recordTypeName": "付款申请",//单据类型
                  "recordCode": "1",//单据号
                  "recordId": "1",//单据id
                  "recordDetailId": "1",//单据明细或者其他细分维度id
                  "topic": "1",//主题
                  "recordDate": "2025-11-27",//单据日期
                  "amount": "1.00",//金额
                  "personId": 1,//单据业务员
                  "personName": "1",//单据业务员
                  "deptId": 1,//部门
                  "deptName": "1",//部门
              "status": "1",//凭证记录状态
                  "success": false,//是否成功
                  "errorReason": "1"//失败原因
              }]
          }
  }
  ```

#### 3.3.2  凭证明细

- **Method**:GET

- **Topic**:/voucher/detail

- **Header**:None

- **Param：**

  | key            | value  | 说明                       | 是否必填 |
  | -------------- | ------ | -------------------------- | -------- |
  | recordId       | string | 单据id                     | 是       |
  | recordDetailId | string | 单据明细或者其他细分维度id |          |

- **Request:**None

- Response:

  ```json
  {
      "errcode":0,
      "msg":"success"
       "data": {
          "total": int,
          "page": null,
          "list": [
              {
                  "cdigest":"摘要",
     			"ccode":"string",//科目编码
                  "ccodeName":"科目名称",
                  "cexchName":"币种",
                  "bproperty":"借贷方向",//科目性质 true为借，false为贷
                  "amount":"金额",
                  "deptName": "1",//部门
             }
          ]
      }
  }
  ```

#### 3.3.3  重新生成凭证

- **Method**:POST

- **Topic**:/voucher

- **Header**:None

- **Param：**

  | key            | value  | 说明                       | 是否必填 |
  | -------------- | ------ | -------------------------- | -------- |
  | moduleType     |        | 单据模块名                 | Y        |
  | recordId       | string | 单据id                     | Y        |
  | recordDetailId | string | 单据明细或者其他细分维度id | 有就传   |

- **Request:**None

- Response:

  ```json
  {
      "errCode": 0,
      "msg": "操作成功",
      "success": true
  }
  ```

#### 