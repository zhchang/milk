grammar MEngine;

options {
  backtrack = true;
  output    = AST;
  memoize   = true;
}

@rulecatch {

}

@header {
package com.mozat.mengine;
}

@lexer::header {
package com.mozat.mengine;
}

program
  :
  LT!* sourceElements LT!* EOF!
  ;

sourceElements
  :
  sourceElement? (LT!* sourceElement)*
  ;

sourceElement
  :
  functionDeclaration
  | statement
  ;

// functions

functionDeclaration
  :
  primeType^ LT!* Identifier LT!* formalParameterList LT!* functionBody
  ;

formalParameterList
  :
  LeftBracket^ (LT!* formalParameter (LT!* ','! LT!* formalParameter)*)? LT!* ')'!
  ;

formalParameter
  :
  (primeType Identifier)
  | (Identifier Identifier)
  ;

functionBody
  :
  LeftCurley^ LT!* sourceElements LT!* '}'!
  ;

// statements

statement
  :
  statementBlock
  | variableStatement
  | emptyStatement
  | expressionStatement
  | ifStatement
  | iterationStatement
  //| continueStatement
  | breakStatement
  | returnStatement
  | switchStatement
  //added statements types here
  //  | callbackStatement
  | cancelTimeOutStatement
  | doDebugStatement
  | debugStatement
  | dotStatement
  | pointStatement
  | pointStatement2
  | pointStatement3
  | sendDataStatement
  | sendDataStatement2
  | sendHttpDataStatement
  | sendHttpDataStatement2
  | sendCommandStatement
  | dbSaveStatement
  | memSaveStatement
  | loadResourceStatement
  | stopSoundStatement
  | loadSoundStatement
  | unloadSoundStatement
  | playSceneStatement
  | setBackgroundStatement
  | constStatement
  | drawStatements
  | maskStatement
  | focusStatement
  //| navigateStatement
  | tryCatchStatement
  | throwStatement
  | openWindowStatement
  | closeWindowStatement
  | closeAllWindowsStatement
  | setMenuStatement
  | openUrlStatement
  | setLoadingScreenStatement
  | openWorldChat
  | openPrivateChat
  | setChatParams
  | sendSms
  | prepareAssets
  | startInput
  | stopInput
  | importStatement
  | initChatTabRect
  | enableShowChatTab
  | typeDefStmt
  ;

TypeDef
  :
  'TYPEDEF'
  ;

typeDefStmt
  :
  TypeDef^ Identifier '('! formalParameter (LT!* ','! LT!* formalParameter)* ')'!
  ;

Transform
  :
  'transform'
  ;

MoveTo
  :
  'moveTo'
  ;

HopTo
  :
  'hopTo'
  ;

MoveBy
  :
  'moveBy'
  ;

RotateTo
  :
  'rotateTo'
  ;

RotateBy
  :
  'rotateBy'
  ;

drawStatements
  :
  draw1ParamStatements
  | draw2ParamStatements
  | draw3ParamStatements
  | draw4ParamStatements
  | draw4ParamStatements
  | draw5ParamStatements
  ;

draw5ParamStatements
  :
  (
    DrawArc
    | FillArc
    | DrawImage
    | DrawRoundRect
    | FillRoundRect
  )^
  '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

DrawArc
  :
  'drawArc'
  ;

FillArc
  :
  'fillArc'
  ;

DrawRoundRect
  :
  'drawRoundRect'
  ;

FillRoundRect
  :
  'fillRoundRect'
  ;

draw4ParamStatements
  :
  (
    DrawRect
    | FillRect
    | DrawLine
    | DrawEclipse
    | FillEclipse
  )^
  '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

DrawLine
  :
  'drawLine'
  ;

draw3ParamStatements
  :
  (DrawImage)^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

draw2ParamStatements
  :
  (
    SetGradientFillColor
    | DrawImage
  )^
  '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

SetGradientFillColor
  :
  'setGradientFillColor'
  ;

OpenWorldChat
  :
  'openWorldChat'
  ;

openWorldChat
  :
  OpenWorldChat^ '('! ')'! ';'!
  ;

OpenPrivateChat
  :
  'openPrivateChat'
  ;

openPrivateChat
  :
  OpenPrivateChat^ '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

SetChatParams
  :
  'setChatParams'
  ;

setChatParams
  :
  SetChatParams^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'
  ;

SendSms
  :
  'sendSms'
  ;

sendSms
  :
  SendSms^ '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

PrepareAssets
  :
  'prepareAssets'
  ;

prepareAssets
  :
  PrepareAssets^ '('! assignmentExpression ')'! ';'!
  ;

StartInput
  :
  'startInput'
  ;

startInput
  :
  StartInput^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

StopInput
  :
  'stopInput'
  ;

stopInput
  :
  StopInput^ '('! ')'! ';'!
  ;

Import
  :
  'import'
  ;

importStatement
  :
  Import^ '('! assignmentExpression ')'! ';'!
  ;

InitChatTabRect
  :
  'initChatTabRect'
  ;

initChatTabRect
  :
  InitChatTabRect^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

EnableShowChatTab
  :
  'enableShowChatTab'
  ;

enableShowChatTab
  :
  EnableShowChatTab^ '('! assignmentExpression ')'! ';'!
  ;

DrawImage
  :
  'drawImage'
  ;

draw1ParamStatements
  :
  (
    SetStrokeColor
    | SetFillColor
    | DrawRect
    | FillRect
    | DrawEclipse
    | FillEclipse
  )^
  '('! assignmentExpression ')'! ';'!
  ;

SetStrokeColor
  :
  'setStrokeColor'
  ;

SetFillColor
  :
  'setFillColor'
  ;

DrawRect
  :
  'drawRect'
  ;

FillRect
  :
  'fillRect'
  ;

DrawEclipse
  :
  'drawEllipse'
  ;

FillEclipse
  :
  'fillEllipse'
  ;

constStatement
  :
  Const^ PTINT! Identifier EQ! NumericLiteral ';'!
  ;

Const
  :
  'const'
  ;

setBackgroundStatement
  :
  SetBackgroundColor^ '('! assignmentExpression ')'! ';'!
  ;

SetBackgroundColor
  :
  'setBackgroundColor'
  ;

playSceneStatement
  :
  PlayScene^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

PlayScene
  :
  'playScene'
  ;

navigateStatement
  :
  Navigate^ '('! assignmentExpression ')'! ';'!
  ;

Navigate
  :
  'navigate'
  ;

focusStatement
  :
  Focus^ '('! assignmentExpression ')'! ';'!
  ;

Focus
  :
  'focus'
  ;

maskStatement
  :
  Mask^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

Mask
  :
  'mask'
  ;

stopSoundStatement
  :
  StopSound^ '('! assignmentExpression ')'! ';'!
  ;

loadSoundStatement
  :
  LoadSound^ '('! assignmentExpression ')'! ';'!
  ;

LoadSound
  :
  'loadSound'
  ;

unloadSoundStatement
  :
  UnloadSound^ '('! assignmentExpression ')'! ';'!
  ;

UnloadSound
  :
  'unloadSound'
  ;

StopSound
  :
  'stopSound'
  ;

dbSaveStatement
  :
  DbSave^ '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

DbSave
  :
  'dbSave'
  ;

loadResourceStatement
  :
  LoadResource^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

LoadResource
  :
  'loadResource'
  ;

memSaveStatement
  :
  MemSave^ '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

MemSave
  :
  'memSave'
  ;

sendDataStatement
  :
  SendData^ '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

SendData
  :
  'sendData'
  ;

sendDataStatement2
  :
  SendData^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

SendHttpData
  :
  'sendHttpData'
  ;

sendHttpDataStatement
  :
  SendHttpData^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

sendHttpDataStatement2
  :
  SendHttpData^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

SendCommand
  :
  'sendCommand'
  ;

sendCommandStatement
  :
  SendCommand^ '('! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

dotStatement
  :
  LT!* dotStmtLeft Dot^ dotStmtRight ';'!
  ;

pointStatement
  :
  LT!* dotStmtLeft Point^ memberExpression arguments ';'!
  ;

pointStatement2
  :
  LT!* pointExp Point^ memberExpression arguments ';'!
  ;

pointStatement3
  :
  LT!* pointExp2 Point^ memberExpression arguments ';'!
  ;

Point
  :
  '->'
  ;

dotStmtLeft
  :
  assignmentExpression
  | global
  | screen
  ;

global
  :
  Global
  ;

Global
  :
  'Global'
  ;

screen
  :
  Screen
  ;

Screen
  :
  'Screen'
  ;

dotStmtRight
  :
  (
    (
      SetX
      | SetY
      | SetViewPort
      | SetWidth
      | SetCells
      | StartAnimation
      | SetClip
      | SetMaxWidth
      | SetHeight
      | SetMaxHeight
      | SetVisible
      | SetZIndex
      | SetText
      | SetFontSize
      | SetFontModifier
      | SetBorderColor
      | SetBgColor
      | SetTextColor
      | SetMaxLines
      | SetAlign
      | SetVerticalAlign
      | SetRect
      | SetState
      | SetFocusable
      | Append
      | Delete
      | OnKeyDown
      | OnKeyUp
      | OnKeyPress
      | OnLeftSoftKey
      | OnRightSoftKey
      | OnFingerDown
      | OnFingerUp
      | OnFingerMove
      | OnFingerZoomOut
      | OnFingerZoomIn
      | OnResourceLoaded
      | OnFrameUpdate
      | OnData
      | OnCommand
      | OnSms
      //| OnWillDraw
      //| OnDidDraw
      | OnFocus
      | OnLostFocus
      | SetData
      | AddChild
      | RemoveChild
      | SetStates
      | Rotate
      | OnClick
    )^
    '('! assignmentExpression ')'!
  )
  | (MoveTo^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!)
  | (HopTo^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!)
  |
  (
    (
      Stop
      | Layout
      | CancelClip
      | StopAnimation
      | Clean
    )^
    '('! ')'!
  )
  |
  (
    (
      Insert
      | DefineState
      | Set
      | Scale
      | SetPivot
      | RotateBy
      | SetAnimatedTile
      | SetIntProperty
      | SetStrProperty
      | InsertAfter
      | InsertBefore
      | SetTileMode
      | MovePos
      | ResizeBounds
    )^
    '('! assignmentExpression ','! assignmentExpression ')'!
  )
  |
  (
    (
      RotateTo
      | MoveBy
      | SetCell
    )^
    '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  )
  | (Transform^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!)
  ;

InsertAfter
  :
  'insertAfter'
  ;

InsertBefore
  :
  'insertBefore'
  ;

SetIntProperty
  :
  'setInt'
  ;

SetStrProperty
  :
  'setStr'
  ;

SetClip
  :
  'setClip'
  ;

CancelClip
  :
  'cancelClip'
  ;

SetCell
  :
  'setCell'
  ;

SetAnimatedTile
  :
  'setAnimatedTile'
  ;

SetPivot
  :
  'setPivot'
  ;

Rotate
  :
  'rotate'
  ;

Scale
  :
  'scale'
  ;

SetStates
  :
  'setStates'
  ;

AddChild
  :
  'addChild'
  ;

RemoveChild
  :
  'removeChild'
  ;

SetData
  :
  'setData'
  ;

OnData
  :
  'onData'
  ;

OnCommand
  :
  'onCommand'
  ;

OnSms
  :
  'onSms'
  ;

//OnWillDraw
//  :
//  'onWillDraw'
//  ;
//
//OnDidDraw
//  :
//  'onDidDraw'
//  ;

SetFocusable
  :
  'setFocusable'
  ;

OnKeyDown
  :
  'onKeyDown'
  ;

OnKeyUp
  :
  'onKeyUp'
  ;

OnKeyPress
  :
  'onKeyPress'
  ;

OnRightSoftKey
  :
  'onRightSoftKey'
  ;

OnLeftSoftKey
  :
  'onLeftSoftKey'
  ;

OnFrameUpdate
  :
  'onFrameUpdate'
  ;

OnFingerDown
  :
  'onFingerDown'
  ;

OnFingerUp
  :
  'onFingerUp'
  ;

OnFingerMove
  :
  'onFingerMove'
  ;

OnFingerZoomIn
  :
  'onFingerZoomIn'
  ;

OnFingerZoomOut
  :
  'onFingerZoomOut'
  ;

OnFocus
  :
  'onFocus'
  ;

OnLostFocus
  :
  'onLostFocus'
  ;

OnClick
  :
  'onClick'
  ;

MovePos
  :
  'movePos'
  ;

ResizeBounds
  :
  'resizeBounds'
  ;

SetTileMode
  :
  'setTileMode'
  ;

OnResourceLoaded
  :
  'onResourceLoaded'
  ;

Stop
  :
  'stop'
  ;

SetState
  :
  'setState'
  ;

DefineState
  :
  'defineState'
  ;

Insert
  :
  'insert'
  ;

SetViewPort
  :
  'setViewPort'
  ;

SetX
  :
  'setX'
  ;

SetY
  :
  'setY'
  ;

SetCells
  :
  'setCells'
  ;

StartAnimation
  :
  'startAnimation'
  ;

Clean
  :
  'clean'
  ;

StopAnimation
  :
  'stopAnimation'
  ;

SetWidth
  :
  'setWidth'
  ;

SetHeight
  :
  'setHeight'
  ;

SetMaxWidth
  :
  'setMaxWidth'
  ;

SetMaxHeight
  :
  'setMaxHeight'
  ;

SetRect
  :
  'setRect'
  ;

SetVisible
  :
  'setVisible'
  ;

SetZIndex
  :
  'setZIndex'
  ;

SetFontSize
  :
  'setFontSize'
  ;

SetFontModifier
  :
  'setFontModifier'
  ;

SetBgColor
  :
  'setBgColor'
  ;

SetBorderColor
  :
  'setBorderColor'
  ;

SetTextColor
  :
  'setTextColor'
  ;

SetText
  :
  'setText'
  ;

SetMaxLines
  :
  'setMaxLines'
  ;

SetAlign
  :
  'setAlign'
  ;

SetVerticalAlign
  :
  'setVerticalAlign'
  ;

Set
  :
  'set'
  ;

Append
  :
  'append'
  ;

Delete
  :
  'delete'
  ;

ToString
  :
  'toStr'
  ;

Layout
  :
  'layout'
  ;

Dot
  :
  '.'
  ;

DoDebug
  :
  'doDebug'
  ;

doDebugStatement
  :
  DoDebug^ '('! ')'! ';'!
  ;

debugStatement
  :
  Debug^ '('! assignmentExpression ')'! ';'!
  ;

Debug
  :
  'debug'
  ;

setTimeOutExpression
  :
  LT!* SetTimeOut^ '('! assignmentExpression ','! assignmentExpression ')'!
  ;

SetTimeOut
  :
  'setTimeOut'
  ;

cancelTimeOutStatement
  :
  LT!* CancelTimeOut^ '('! assignmentExpression ')'! ';'!
  ;

CancelTimeOut
  :
  'cancelTimeOut'
  ;

//callbackStatement
//  :
//  LT!* OnFrameCallback^ '('! Identifier ')'! ';'!
//  ;
//
//OnFrameCallback
//  :
//  'onFrameCallback'
//  ;

statementBlock
  :
  LeftCurley^ LT!* statementList? LT!* '}'!
  ;

LeftCurley
  :
  '{'
  ;

statementList
  :
  statement (LT!* statement)*
  ;

variableStatement
  :
  variableDeclareExpression ';'!
  ;

variableDeclareExpression
  :
  primeType^ variableDeclarationList
  | Define^ Identifier Identifier
  ;

Define
  :
  'define'
  ;

primeType
  :
  PTINT
  | PTSTRING
  | PTARRAY
  | PTMAP
  | PTVOID
  | PTRECT
  | PTPLAYER
  | PTTEXT
  | PTGROUP
  | PTTILES
  | PTELEMENT
  | PTPAGE
  ;

PTTILES
  :
  'MTiles'
  ;

PTGROUP
  :
  'MGroup'
  ;

PTRECT
  :
  'MRect'
  ;

PTPLAYER
  :
  'MPlayer'
  ;

PTTEXT
  :
  'MText'
  ;

PTVOID
  :
  'void'
  ;

PTINT
  :
  'int'
  ;

PTPAGE
  :
  'Page'
  ;

PTELEMENT
  :
  'Element'
  ;

PTSTRING
  :
  'String'
  ;

PTARRAY
  :
  'Array'
  ;

PTMAP
  :
  'Map'
  ;

variableDeclarationList
  :
  variableDeclaration (LT!* ',' LT!* variableDeclaration)*
  ;

variableDeclaration
  :
  Identifier LT!* initialiser?
  ;

initialiser
  :
  EQ^ LT!* assignmentExpression
  ;

emptyStatement
  :
  Empty^
  ;

Empty
  :
  ';'
  ;

expressionStatement
  :
  expression ';'!
  ;

ifStatement
  :
  If^ LT!* '('! LT!* expression LT!* ')'! LT!* statement (LT!* 'else'! LT!* statement)?
  ;

If
  :
  'if'
  ;

tryCatchStatement
  :
  Try^ LT!* statementBlock LT!* Catch! LT!* '('! expression ')'! LT!* statementBlock
  ;

Try
  :
  'try'
  ;

Catch
  :
  'catch'
  ;

CreateElement
  :
  'createElement'
  ;

createElementExpression
  :
  CreateElement^ '('! assignmentExpression ','! assignmentExpression ')'!
  ;

getElementById
  :
  GetElementById^ '('! assignmentExpression ')'!
  ;

getPage
  :
  GetPage^ '('! ')'!
  ;

getRoot
  :
  GetRoot^ '('! ')'!
  ;

OpenUrl
  :
  'openUrl'
  ;

openUrlStatement
  :
  OpenUrl^ '('! assignmentExpression ')'! ';'!
  ;

setLoadingScreenStatement
  :
  SetLoadingScreen^ '('! assignmentExpression ')'! ';'!
  ;

SetLoadingScreen
  :
  'setLoadingScreen'
  ;

SetMenus
  :
  'setMenus'
  ;

setMenuStatement
  :
  SetMenus^ '('! assignmentExpression ')'! ';'!

  //| HexIntegerLiteral
  ;

OpenWindow
  :
  'openWindow'
  ;

openWindowStatement
  :
  OpenWindow^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'! ';'!
  ;

CloseWindow
  :
  'closeWindow'
  ;

closeWindowStatement
  :
  CloseWindow^ '('! assignmentExpression ')'! ';'!
  ;

CloseAllWindows
  :
  'closeAllWindows'
  ;

closeAllWindowsStatement
  :
  CloseAllWindows^ '('! ')'! ';'!
  ;

throwStatement
  :
  Throw^ expression ';'!
  ;

Throw
  :
  'throw'
  ;

iterationStatement
  :
  forStatement
  ;

forStatement
  :
  For^ '(' forInitExp? ';' (expression)? ';' (expression)? ')' LT!* statement
  ;

forInitExp
  :
  variableDeclareExpression
  | expression
  ;

For
  :
  'for'
  ;

continueStatement
  :
  Continue^
  (
    LT
    | ';'
  )!
  ;

Continue
  :
  'continue'
  ;

breakStatement
  :
  Break^ ';'!
  ;

Break
  :
  'break'
  ;

returnStatement
  :
  Return^ expression? ';'!
  ;

Return
  :
  'return'
  ;

switchStatement
  :
  Switch^ LT!* '('! LT!* expression LT!* ')'! LT!* caseBlock
  ;

Switch
  :
  'switch'
  ;

caseBlock
  :
  LeftCurley^ (LT!* caseClause)* (LT!* defaultClause (LT!* caseClause)*)? LT!* '}'!
  ;

caseClause
  :
  Case^ LT!*
  (
    NumericLiteral
    | Identifier
  )
  LT!* ':'! LT!* statementList?
  ;

Case
  :
  'case'
  ;

defaultClause
  :
  Default^ LT!* ':'! LT!* statementList?
  ;

Default
  :
  'default'
  ;

expression
  :
  assignmentExpression (LT!* ',' LT!* assignmentExpression)*
  ;

assignmentExpression
  :
  conditionalExpression
  | leftHandSideExpression LT!* assignmentOperator^ LT!* assignmentExpression
  ;

dotExpression
  :
  dotExpLeftValues Dot^ dotExpRightValues
  ;

pointExp
  :
  dotExpLeftValues Point^ Identifier '('! ')'!
  ;

pointExpII
  :
  pointExp Point^ Identifier '('! ')'!
  ;

pointExpII2
  :
  pointExp Point^ memberExpression arguments
  ;

pointExpIII
  :
  pointExp2 Point^ Identifier '('! ')'!
  ;

pointExpIII2
  :
  pointExp2 Point^ memberExpression arguments
  ;

pointExp2
  :
  dotExpLeftValues Point^ memberExpression arguments
  ;

Point2
  :
  '=>'
  ;

dotExpression2
  :
  dotExpression Dot^ dotExpRightValues
  ;

dotExpression3
  :
  dotExpression2 Dot^ dotExpRightValues
  ;

dotExpLeftValues
  :
  leftHandSideExpression
  ;

dotExpRightValues
  :
  (
    (
      GetX
      | GetY
      | GetViewPort
      | GetWidth
      | GetClip
      | GetMaxWidth
      | GetHeight
      | GetMaxHeight
      | GetState
      | GetZIndex
      | GetVisible
      | GetRect
      | GetSize
      | GetText
      | GetFontSize
      | GetFontModifier
      | GetBgColor
      | GetBgTransparent
      | GetBorderColor
      | GetTextColor
      | GetMaxLines
      | GetAlign
      | GetVerticalAlign
      | GetFocusable
      | IsNull
      | NotNull
      | Length
      | ToUpper
      | ToLower
      | ToInt
      | GetLayoutWidth
      | GetLayoutHeight
      | ToString
      | GetData
      | GetParent
      | GetChildren
      | GetPivotX
      | GetPivotY
      | GetRotateDegree
      | GetScaleX
      | GetScaleY
      | GetKeys
    )^
    '('! ')'!
  )
  |
  (
    (
      HasKey
      | HasValue
      | GetType
      | GetInt
      | GetString
      | GetArray
      | GetMap
      | GetRect
      | GetPlayer
      | GetTextObj
      | GetGroup
      | GetElement
      | GetTiles
      | Compare
      | CreateAnimatedTile
      | GetAnimatedTile
      | MakeCopy
      | Intersacts
      | ContainsRect
      | GetGlobalX
      | GetGlobalY
      | GetLocalX
      | GetLocalY
    )^
    '('! assignmentExpression ')'!
  )
  |
  (
    (
      SubString
      | Replace
      | GetLocalPoint
      | GetGlobalPoint
      | GetCell
      | Split
      | MatchFingerToCell
      | MatchCellToCoord
      | ContainsPoint
    )^
    '('! assignmentExpression ','! assignmentExpression ')'!
  )
  | (IndexOf^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!)
  |
  (
    (
      ContainsRect
      | Intersacts
    )^
    '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  )
  ;

Intersacts
  :
  'intersacts'
  ;

ContainsRect
  :
  'containsRect'
  ;

ContainsPoint
  :
  'containsPoint'
  ;

MatchFingerToCell
  :
  'matchFingerToCell'
  ;

MatchCellToCoord
  :
  'matchCellToCoord'
  ;

Split
  :
  'split'
  ;

MakeCopy
  :
  'makeCopy'
  ;

GetPage
  :
  'getPage'
  ;

GetRoot
  :
  'getRoot'
  ;

GetElementById
  :
  'getElementById'
  ;

GetCell
  :
  'getCell'
  ;

CreateAnimatedTile
  :
  'createAnimatedTile'
  ;

GetAnimatedTile
  :
  'getAnimatedTile'
  ;

GetState
  :
  'getState'
  ;

GetPivotX
  :
  'getPivotX'
  ;

GetPivotY
  :
  'getPivotY'
  ;

GetRotateDegree
  :
  'getRotateDegree'
  ;

GetScaleX
  :
  'getScaleX'
  ;

GetScaleY
  :
  'getScaleY'
  ;

GetParent
  :
  'getParent'
  ;

GetChildren
  :
  'getChildren'
  ;

GetLocalPoint
  :
  'getLocalPoint'
  ;

GetGlobalPoint
  :
  'getGlobalPoint'
  ;

GetGlobalX
  :
  'getGlobalX'
  ;

GetGlobalY
  :
  'getGlobalY'
  ;

GetLocalX
  :
  'getLocalX'
  ;

GetLocalY
  :
  'getLocalY'
  ;

GetData
  :
  'getData'
  ;

ToInt
  :
  'toInt'
  ;

ToLower
  :
  'toLower'
  ;

ToUpper
  :
  'toUpper'
  ;

Length
  :
  'length'
  ;

Compare
  :
  'compare'
  ;

IndexOf
  :
  'indexOf'
  ;

SubString
  :
  'subString'
  ;

Replace
  :
  'replace'
  ;

IsNull
  :
  'isNull'
  ;

NotNull
  :
  'notNull'
  ;

GetFocusable
  :
  'getFocusable'
  ;

HasValue
  :
  'hasValue'
  ;

HasKey
  :
  'hasKey'
  ;

GetType
  :
  'getType'
  ;

GetSize
  :
  'getSize'
  ;

GetViewPort
  :
  'getViewPort'
  ;

GetX
  :
  'getX'
  ;

GetY
  :
  'getY'
  ;

GetClip
  :
  'getClip'
  ;

GetWidth
  :
  'getWidth'
  ;

GetHeight
  :
  'getHeight'
  ;

GetMaxWidth
  :
  'getMaxWidth'
  ;

GetMaxHeight
  :
  'getMaxHeight'
  ;

GetLayoutWidth
  :
  'getLayoutWidth'
  ;

GetLayoutHeight
  :
  'getLayoutHeight'
  ;

GetZIndex
  :
  'getZIndex'
  ;

GetVisible
  :
  'getVisible'
  ;

GetRect
  :
  'getRect'
  ;

GetInt
  :
  'getInt'
  ;

GetString
  :
  'getString'
  ;

GetArray
  :
  'getArray'
  ;

GetMap
  :
  'getMap'
  ;

GetKeys
  :
  'getKeys'
  ;

GetPlayer
  :
  'getPlayer'
  ;

GetTextObj
  :
  'getTextObj'
  ;

GetGroup
  :
  'getGroup'
  ;

GetElement
  :
  'getElement'
  ;

GetTiles
  :
  'getTiles'
  ;

GetText
  :
  'getText'
  ;

GetFontSize
  :
  'getFontSize'
  ;

GetFontModifier
  :
  'getFontModifier'
  ;

GetBgColor
  :
  'getBgColor'
  ;

GetBgTransparent
  :
  'getBgTransparent'
  ;

GetBorderColor
  :
  'getBorderColor'
  ;

GetTextColor
  :
  'getTextColor'
  ;

GetMaxLines
  :
  'getMaxLines'
  ;

GetAlign
  :
  'getAlign'
  ;

GetVerticalAlign
  :
  'getVerticalAlign'
  ;

getMyUserId
  :
  GetMyUserId^ '('! ')'!
  ;

GetMyUserId
  :
  'getMyUserId'
  ;

getStartParams
  :
  GetStartParams^ '('! ')'!
  ;

GetStartParams
  :
  'getStartParams'
  ;

translate
  :
  Translate^ '('! assignmentExpression ')'!
  ;

translate2
  :
  Translate^ '('! assignmentExpression ','! assignmentExpression ')'!
  ;

Translate
  :
  'translate'
  ;

GetLanguage
  :
  'getLanguage'
  ;

getLanguage
  :
  GetLanguage^ '('! ')'!
  ;

GetUsername
  :
  'getUsername'
  ;

getUsername
  :
  GetUsername^ '('! ')'!
  ;

GetPassword
  :
  'getPassword'
  ;

getPassword
  :
  GetPassword^ '('! ')'!
  ;

GetVersion
  :
  'getVersion'
  ;

getVersion
  :
  GetVersion^ '('! ')'!
  ;

GetRequiredVersion
  :
  'getRequiredVersion'
  ;

getRequiredVersion
  :
  GetRequiredVersion^ '('! ')'!
  ;

GetAutoRegParams
  :
  'getAutoRegParams'
  ;

getAutoRegParams
  :
  GetAutoRegParams^ '('! ')'!
  ;

getAutoRegParams2
  :
  GetAutoRegParams^ '('! assignmentExpression ','! assignmentExpression ')'!
  ;

getEnvVar
  :
  GetEnvVar^ '('! assignmentExpression ')'!
  ;

GetEnvVar
  :
  'getEnvVar'
  ;

getScreenWidth
  :
  GetScreenWidth^ '('! ')'!
  ;

GetScreenWidth
  :
  'getScreenWidth'
  ;

getScreenHeight
  :
  GetScreenHeight^ '('! ')'!
  ;

GetScreenHeight
  :
  'getScreenHeight'
  ;

getPlatform
  :
  GetPlatform^ '('! ')'!
  ;

GetPlatform
  :
  'getPlatform'
  ;

isTouchSupported
  :
  IsTouchSupported^ '('! ')'!
  ;

IsTouchSupported
  :
  'isTouchSupported'
  ;

getImageWidth
  :
  GetImageWidth^ '('! assignmentExpression ')'!
  ;

GetImageWidth
  :
  'getImageWidth'
  ;

getImageHeight
  :
  GetImageHeight^ '('! assignmentExpression ')'!
  ;

GetImageHeight
  :
  'getImageHeight'
  ;

aRGB
  :
  ARGB^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  ;

ARGB
  :
  'ARGB'
  ;

rGB
  :
  RGB^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  ;

RGB
  :
  'RGB'
  ;

randExpression
  :
  RandExp^ '('! assignmentExpression ','! assignmentExpression ')'!
  ;

RandExp
  :
  'random'
  ;

objectInitExpression
  :
  initPlayerExpression
  | initRectExpression
  | initTextExpression
  | initGroupExpression
  | initTilesExpression
  | initTilesExpression2
  | initArrayExpression
  | initMapExpression
  ;

initTilesExpression
  :
  InitTiles^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  ;

initTilesExpression2
  :
  InitTiles^ '('! assignmentExpression ')'!
  ;

InitTiles
  :
  'initTiles'
  ;

initGroupExpression
  :
  InitGroup^ '('! assignmentExpression (','! assignmentExpression)* ')'!
  ;

InitGroup
  :
  'initGroup'
  ;

InitArray
  :
  'initArray'
  ;

initArrayExpression
  :
  InitArray^ '('! ')'!
  ;

InitMap
  :
  'initMap'
  ;

initMapExpression
  :
  InitMap^ '('! ')'!
  ;

initPlayerExpression
  :
  InitPlayer^ '('! assignmentExpression (','! assignmentExpression)* ')'!
  ;

InitPlayer
  :
  'initPlayer'
  ;

initRectExpression
  :
  InitRect^ '('! assignmentExpression (','! assignmentExpression)* ')'!
  ;

InitRect
  :
  'initRect'
  ;

initTextExpression
  :
  InitText^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  ;

InitText
  :
  'initText'
  ;

getLoadingProgress
  :
  GetLoadingProgress^ '('! ')'!
  ;

GetLoadingProgress
  :
  'getLoadingProgress'
  ;

GetQuickInput
  :
  'getQuickInput'
  ;

getQuickInput
  :
  GetQuickInput^ '('! assignmentExpression ','! assignmentExpression ','! assignmentExpression ','! assignmentExpression ')'!
  ;

getTimeExpression
  :
  GetTimeStamp^ '('! ')'!
  ;

GetTimeStamp
  :
  'getTimeStamp'
  ;

getTimeElapsedExpression
  :
  GetTimeElapsedInMilliseconds^ '('! ')'!
  ;

GetTimeElapsedInMilliseconds
  :
  'getTimeElapsedInMilliseconds'
  ;

leftHandSideExpression
  :
  callExpression
  | newExpression
  ;

newExpression
  :
  memberExpression
  ;

memberExpression
  :
  (primaryExpression)
  ;

memberExpressionSuffix
  :
  indexSuffix
  | propertyReferenceSuffix
  ;

callExpression
  :
  memberExpression^ LT!* arguments (LT!* callExpressionSuffix)*
  ;

Call
  :
  'call'
  ;

callExpressionSuffix
  :
  arguments
  | indexSuffix
  | propertyReferenceSuffix
  ;

arguments
  :
  LeftBracket^ (LT!* assignmentExpression (LT!* ','! LT!* assignmentExpression)*)? LT!* ')'!
  ;

LeftBracket
  :
  '('
  ;

indexSuffix
  :
  '[' LT!* expression LT!* ']'
  ;

propertyReferenceSuffix
  :
  '.' LT!* Identifier
  ;

assignmentOperator
  :
  EQ
  | MultiplyEQ
  | DivideEQ
  | ModEQ
  | AddEQ
  | MinusEQ
  | LeftShiftEQ
  | RightShiftEQ
  | '>>>='
  | AndEQ
  | TimesEQ
  | OrEQ
  ;

EQ
  :
  '='
  ;

MultiplyEQ
  :
  '*='
  ;

DivideEQ
  :
  '/='
  ;

ModEQ
  :
  '%='
  ;

AddEQ
  :
  '+='
  ;

MinusEQ
  :
  '-='
  ;

LeftShiftEQ
  :
  '<<='
  ;

RightShiftEQ
  :
  '>>='
  ;

AndEQ
  :
  '&='
  ;

OrEQ
  :
  '|='
  ;

TimesEQ
  :
  '^='
  ;

conditionalExpression
  :
  logicalORExpression (LT!* '?'^ LT!* assignmentExpression LT!* ':' LT!* assignmentExpression)?
  ;

logicalORExpression
  :
  logicalANDExpression (LT!* LogicalOr^ LT!* logicalANDExpression)*
  ;

LogicalOr
  :
  '||'
  ;

logicalANDExpression
  :
  bitwiseORExpression (LT!* LogicalAnd^ LT!* bitwiseORExpression)*
  ;

LogicalAnd
  :
  '&&'
  ;

bitwiseORExpression
  :
  bitwiseXORExpression (LT!* BitOr^ LT!* bitwiseXORExpression)*
  ;

BitOr
  :
  '|'
  ;

bitwiseXORExpression
  :
  bitwiseANDExpression (LT!* BitXor^ LT!* bitwiseANDExpression)*
  ;

BitXor
  :
  '^'
  ;

bitwiseANDExpression
  :
  equalityExpression (LT!* BitAnd^ LT!* equalityExpression)*
  ;

BitAnd
  :
  '&'
  ;

equalityExpression
  :
  relationalExpression
  (
    LT!*
    (
      EqEq
      | NotEq
      | '==='
      | '!=='
    )^
    LT!* relationalExpression
  )*
  ;

EqEq
  :
  '=='
  ;

NotEq
  :
  '!='
  ;

LessThan
  :
  '<'
  ;

GreatThan
  :
  '>'
  ;

LessOrEq
  :
  '<='
  ;

GreatOrEq
  :
  '>='
  ;

relationalExpression
  :
  shiftExpression
  (
    LT!*
    (
      LessThan
      | GreatThan
      | LessOrEq
      | GreatOrEq
      | 'instanceof'
      | 'in'
    )^
    LT!* shiftExpression
  )*
  ;

shiftExpression
  :
  additiveExpression
  (
    LT!*
    (
      LeftShift
      | RightShift
      | '>>>'
    )^
    LT!* additiveExpression
  )*
  ;

LeftShift
  :
  '<<'
  ;

RightShift
  :
  '>>'
  ;

additiveExpression
  :
  multiplicativeExpression
  (
    LT!*
    (
      Add
      | Minus
    )^
    LT!* multiplicativeExpression
  )*
  ;

Add
  :
  '+'
  ;

Minus
  :
  '-'
  ;

Multiply
  :
  '*'
  ;

Divide
  :
  '/'
  ;

Mod
  :
  '%'
  ;

multiplicativeExpression
  :
  unaryExpression
  (
    LT!*
    (
      Multiply
      | Divide
      | Mod
    )^
    LT!* unaryExpression
  )*
  ;

unaryExpression
  :
  dbLoadExpression
  | playSoundExpression
  | translate
  | translate2
  | getLanguage
  | getVersion
  | getRequiredVersion
  | getAutoRegParams
  | getAutoRegParams2
  | getScreenWidth
  | getScreenHeight
  | getPlatform
  | getUsername
  | getPassword
  | isTouchSupported
  | getMyUserId
  | getStartParams
  | getImageWidth
  | getImageHeight
  | getEnvVar
  | aRGB
  | rGB
  | objectInitExpression
  | randExpression
  | getQuickInput
  | getTimeExpression
  | getTimeElapsedExpression
  | getLoadingProgress
  | memLoadExpression
  | pointExp
  | pointExp2
  | pointExpII
  | pointExpII2
  | pointExpIII
  | pointExpIII2
  | dotExpression3
  | dotExpression2
  | dotExpression
  | setTimeOutExpression
  | postfixExpression
  | getScreenExpression
  | getCurrentFocus
  | createElementExpression
  | getElementById
  | getPage
  | getRoot
  |
  (
    'delete'
    | 'void'
    | 'typeof'
    | '++'
    | '--'
    | '+'
    | '-'
    | '~'
    | '!'
  )^
  unaryExpression
  ;

getCurrentFocus
  :
  GetCurrentFocus^ '('! ')'!
  ;

GetCurrentFocus
  :
  'getCurrentFocus'
  ;

getScreenExpression
  :
  GetScreenExp^ '('! ')'!
  ;

GetScreenExp
  :
  'getScreen'
  ;

playSoundExpression
  :
  PlaySound^ '('! assignmentExpression ','! assignmentExpression ')'!
  ;

PlaySound
  :
  'playSound'
  ;

dbLoadExpression
  :
  DbLoad^ '('! assignmentExpression ')'!
  ;

DbLoad
  :
  'dbLoad'
  ;

memLoadExpression
  :
  MemLoad^ '('! assignmentExpression ')'!
  ;

MemLoad
  :
  'memLoad'
  ;

postfixExpression
  :
  leftHandSideExpression
  (
    (
      PlusPlus
      | MinusMinus
    )^
  )?
  ;

PlusPlus
  :
  '++'
  ;

MinusMinus
  :
  '--'
  ;

primaryExpression
  :
  'this'
  | Identifier
  | literal
  //| arrayLiteral
  //| objectLiteral
  | bracketExp
  ;

bracketExp
  :
  LeftBracket^ expression ')'!
  ;

arrayLiteral
  :
  '[' LT!* assignmentExpression? (LT!* ',' (LT!* assignmentExpression)?)* LT!* ']'
  ;

objectLiteral
  :
  '{' LT!* propertyNameAndValue (LT!* ',' LT!* propertyNameAndValue)* LT!* '}'
  ;

propertyNameAndValue
  :
  propertyName LT!* ':' LT!* assignmentExpression
  ;

propertyName
  :
  Identifier
  | StringLiteral
  | NumericLiteral
  ;

literal
  :
  StringLiteral
  | NumericLiteral
  ;

LeftHardBracket
  :
  '['
  ;

Colon
  :
  ':'
  ;

StringLiteral
  :
  '"' DoubleStringCharacter* '"'
  | '\'' SingleStringCharacter* '\''
  ;

fragment
DoubleStringCharacter
  :
  ~(
    '"'
    | '\\'
    | LT
   )
  | '\\' EscapeSequence
  ;

fragment
SingleStringCharacter
  :
  ~(
    '\''
    | '\\'
    | LT
   )
  | '\\' EscapeSequence
  ;

fragment
EscapeSequence
  :
  CharacterEscapeSequence
  | '0'
  | HexEscapeSequence
  | UnicodeEscapeSequence
  ;

fragment
CharacterEscapeSequence
  :
  SingleEscapeCharacter
  | NonEscapeCharacter
  ;

fragment
NonEscapeCharacter
  :
  ~(
    EscapeCharacter
    | LT
   )
  ;

fragment
SingleEscapeCharacter
  :
  '\''
  | '"'
  | '\\'
  | 'b'
  | 'f'
  | 'n'
  | 'r'
  | 't'
  | 'v'
  ;

fragment
EscapeCharacter
  :
  SingleEscapeCharacter
  | DecimalDigit
  | 'x'
  | 'u'
  ;

fragment
HexEscapeSequence
  :
  'x' HexDigit HexDigit
  ;

fragment
UnicodeEscapeSequence
  :
  'u' HexDigit HexDigit HexDigit HexDigit
  ;

NumericLiteral
  :
  '-'? DecimalLiteral
  ;

fragment
HexIntegerLiteral
  :
  '0'
  (
    'x'
    | 'X'
  )
  HexDigit+
  ;

fragment
HexDigit
  :
  DecimalDigit
  | ('a'..'f')
  | ('A'..'F')
  ;

fragment
DecimalLiteral
  :
  DecimalDigit+ '.' DecimalDigit* ExponentPart?
  | '.'? DecimalDigit+ ExponentPart?
  ;

fragment
DecimalDigit
  :
  ('0'..'9')
  ;

fragment
ExponentPart
  :
  (
    'e'
    | 'E'
  )
  (
    '+'
    | '-'
  )?
  DecimalDigit+
  ;

Identifier
  :
  IdentifierStart IdentifierPart*
  ;

fragment
IdentifierStart
  :
  UnicodeLetter
  | '$'
  | '_'
  | '\\' UnicodeEscapeSequence
  ;

fragment
IdentifierPart
  :
  (IdentifierStart) => IdentifierStart
  | UnicodeDigit
  | UnicodeConnectorPunctuation
  ;

fragment
UnicodeLetter
  :
  '\u0041'..'\u005A'
  | '\u0061'..'\u007A'
  | '\u00AA'
  | '\u00B5'
  | '\u00BA'
  | '\u00C0'..'\u00D6'
  | '\u00D8'..'\u00F6'
  | '\u00F8'..'\u021F'
  | '\u0222'..'\u0233'
  | '\u0250'..'\u02AD'
  | '\u02B0'..'\u02B8'
  | '\u02BB'..'\u02C1'
  | '\u02D0'..'\u02D1'
  | '\u02E0'..'\u02E4'
  | '\u02EE'
  | '\u037A'
  | '\u0386'
  | '\u0388'..'\u038A'
  | '\u038C'
  | '\u038E'..'\u03A1'
  | '\u03A3'..'\u03CE'
  | '\u03D0'..'\u03D7'
  | '\u03DA'..'\u03F3'
  | '\u0400'..'\u0481'
  | '\u048C'..'\u04C4'
  | '\u04C7'..'\u04C8'
  | '\u04CB'..'\u04CC'
  | '\u04D0'..'\u04F5'
  | '\u04F8'..'\u04F9'
  | '\u0531'..'\u0556'
  | '\u0559'
  | '\u0561'..'\u0587'
  | '\u05D0'..'\u05EA'
  | '\u05F0'..'\u05F2'
  | '\u0621'..'\u063A'
  | '\u0640'..'\u064A'
  | '\u0671'..'\u06D3'
  | '\u06D5'
  | '\u06E5'..'\u06E6'
  | '\u06FA'..'\u06FC'
  | '\u0710'
  | '\u0712'..'\u072C'
  | '\u0780'..'\u07A5'
  | '\u0905'..'\u0939'
  | '\u093D'
  | '\u0950'
  | '\u0958'..'\u0961'
  | '\u0985'..'\u098C'
  | '\u098F'..'\u0990'
  | '\u0993'..'\u09A8'
  | '\u09AA'..'\u09B0'
  | '\u09B2'
  | '\u09B6'..'\u09B9'
  | '\u09DC'..'\u09DD'
  | '\u09DF'..'\u09E1'
  | '\u09F0'..'\u09F1'
  | '\u0A05'..'\u0A0A'
  | '\u0A0F'..'\u0A10'
  | '\u0A13'..'\u0A28'
  | '\u0A2A'..'\u0A30'
  | '\u0A32'..'\u0A33'
  | '\u0A35'..'\u0A36'
  | '\u0A38'..'\u0A39'
  | '\u0A59'..'\u0A5C'
  | '\u0A5E'
  | '\u0A72'..'\u0A74'
  | '\u0A85'..'\u0A8B'
  | '\u0A8D'
  | '\u0A8F'..'\u0A91'
  | '\u0A93'..'\u0AA8'
  | '\u0AAA'..'\u0AB0'
  | '\u0AB2'..'\u0AB3'
  | '\u0AB5'..'\u0AB9'
  | '\u0ABD'
  | '\u0AD0'
  | '\u0AE0'
  | '\u0B05'..'\u0B0C'
  | '\u0B0F'..'\u0B10'
  | '\u0B13'..'\u0B28'
  | '\u0B2A'..'\u0B30'
  | '\u0B32'..'\u0B33'
  | '\u0B36'..'\u0B39'
  | '\u0B3D'
  | '\u0B5C'..'\u0B5D'
  | '\u0B5F'..'\u0B61'
  | '\u0B85'..'\u0B8A'
  | '\u0B8E'..'\u0B90'
  | '\u0B92'..'\u0B95'
  | '\u0B99'..'\u0B9A'
  | '\u0B9C'
  | '\u0B9E'..'\u0B9F'
  | '\u0BA3'..'\u0BA4'
  | '\u0BA8'..'\u0BAA'
  | '\u0BAE'..'\u0BB5'
  | '\u0BB7'..'\u0BB9'
  | '\u0C05'..'\u0C0C'
  | '\u0C0E'..'\u0C10'
  | '\u0C12'..'\u0C28'
  | '\u0C2A'..'\u0C33'
  | '\u0C35'..'\u0C39'
  | '\u0C60'..'\u0C61'
  | '\u0C85'..'\u0C8C'
  | '\u0C8E'..'\u0C90'
  | '\u0C92'..'\u0CA8'
  | '\u0CAA'..'\u0CB3'
  | '\u0CB5'..'\u0CB9'
  | '\u0CDE'
  | '\u0CE0'..'\u0CE1'
  | '\u0D05'..'\u0D0C'
  | '\u0D0E'..'\u0D10'
  | '\u0D12'..'\u0D28'
  | '\u0D2A'..'\u0D39'
  | '\u0D60'..'\u0D61'
  | '\u0D85'..'\u0D96'
  | '\u0D9A'..'\u0DB1'
  | '\u0DB3'..'\u0DBB'
  | '\u0DBD'
  | '\u0DC0'..'\u0DC6'
  | '\u0E01'..'\u0E30'
  | '\u0E32'..'\u0E33'
  | '\u0E40'..'\u0E46'
  | '\u0E81'..'\u0E82'
  | '\u0E84'
  | '\u0E87'..'\u0E88'
  | '\u0E8A'
  | '\u0E8D'
  | '\u0E94'..'\u0E97'
  | '\u0E99'..'\u0E9F'
  | '\u0EA1'..'\u0EA3'
  | '\u0EA5'
  | '\u0EA7'
  | '\u0EAA'..'\u0EAB'
  | '\u0EAD'..'\u0EB0'
  | '\u0EB2'..'\u0EB3'
  | '\u0EBD'..'\u0EC4'
  | '\u0EC6'
  | '\u0EDC'..'\u0EDD'
  | '\u0F00'
  | '\u0F40'..'\u0F6A'
  | '\u0F88'..'\u0F8B'
  | '\u1000'..'\u1021'
  | '\u1023'..'\u1027'
  | '\u1029'..'\u102A'
  | '\u1050'..'\u1055'
  | '\u10A0'..'\u10C5'
  | '\u10D0'..'\u10F6'
  | '\u1100'..'\u1159'
  | '\u115F'..'\u11A2'
  | '\u11A8'..'\u11F9'
  | '\u1200'..'\u1206'
  | '\u1208'..'\u1246'
  | '\u1248'
  | '\u124A'..'\u124D'
  | '\u1250'..'\u1256'
  | '\u1258'
  | '\u125A'..'\u125D'
  | '\u1260'..'\u1286'
  | '\u1288'
  | '\u128A'..'\u128D'
  | '\u1290'..'\u12AE'
  | '\u12B0'
  | '\u12B2'..'\u12B5'
  | '\u12B8'..'\u12BE'
  | '\u12C0'
  | '\u12C2'..'\u12C5'
  | '\u12C8'..'\u12CE'
  | '\u12D0'..'\u12D6'
  | '\u12D8'..'\u12EE'
  | '\u12F0'..'\u130E'
  | '\u1310'
  | '\u1312'..'\u1315'
  | '\u1318'..'\u131E'
  | '\u1320'..'\u1346'
  | '\u1348'..'\u135A'
  | '\u13A0'..'\u13B0'
  | '\u13B1'..'\u13F4'
  | '\u1401'..'\u1676'
  | '\u1681'..'\u169A'
  | '\u16A0'..'\u16EA'
  | '\u1780'..'\u17B3'
  | '\u1820'..'\u1877'
  | '\u1880'..'\u18A8'
  | '\u1E00'..'\u1E9B'
  | '\u1EA0'..'\u1EE0'
  | '\u1EE1'..'\u1EF9'
  | '\u1F00'..'\u1F15'
  | '\u1F18'..'\u1F1D'
  | '\u1F20'..'\u1F39'
  | '\u1F3A'..'\u1F45'
  | '\u1F48'..'\u1F4D'
  | '\u1F50'..'\u1F57'
  | '\u1F59'
  | '\u1F5B'
  | '\u1F5D'
  | '\u1F5F'..'\u1F7D'
  | '\u1F80'..'\u1FB4'
  | '\u1FB6'..'\u1FBC'
  | '\u1FBE'
  | '\u1FC2'..'\u1FC4'
  | '\u1FC6'..'\u1FCC'
  | '\u1FD0'..'\u1FD3'
  | '\u1FD6'..'\u1FDB'
  | '\u1FE0'..'\u1FEC'
  | '\u1FF2'..'\u1FF4'
  | '\u1FF6'..'\u1FFC'
  | '\u207F'
  | '\u2102'
  | '\u2107'
  | '\u210A'..'\u2113'
  | '\u2115'
  | '\u2119'..'\u211D'
  | '\u2124'
  | '\u2126'
  | '\u2128'
  | '\u212A'..'\u212D'
  | '\u212F'..'\u2131'
  | '\u2133'..'\u2139'
  | '\u2160'..'\u2183'
  | '\u3005'..'\u3007'
  | '\u3021'..'\u3029'
  | '\u3031'..'\u3035'
  | '\u3038'..'\u303A'
  | '\u3041'..'\u3094'
  | '\u309D'..'\u309E'
  | '\u30A1'..'\u30FA'
  | '\u30FC'..'\u30FE'
  | '\u3105'..'\u312C'
  | '\u3131'..'\u318E'
  | '\u31A0'..'\u31B7'
  | '\u3400'
  | '\u4DB5'
  | '\u4E00'
  | '\u9FA5'
  | '\uA000'..'\uA48C'
  | '\uAC00'
  | '\uD7A3'
  | '\uF900'..'\uFA2D'
  | '\uFB00'..'\uFB06'
  | '\uFB13'..'\uFB17'
  | '\uFB1D'
  | '\uFB1F'..'\uFB28'
  | '\uFB2A'..'\uFB36'
  | '\uFB38'..'\uFB3C'
  | '\uFB3E'
  | '\uFB40'..'\uFB41'
  | '\uFB43'..'\uFB44'
  | '\uFB46'..'\uFBB1'
  | '\uFBD3'..'\uFD3D'
  | '\uFD50'..'\uFD8F'
  | '\uFD92'..'\uFDC7'
  | '\uFDF0'..'\uFDFB'
  | '\uFE70'..'\uFE72'
  | '\uFE74'
  | '\uFE76'..'\uFEFC'
  | '\uFF21'..'\uFF3A'
  | '\uFF41'..'\uFF5A'
  | '\uFF66'..'\uFFBE'
  | '\uFFC2'..'\uFFC7'
  | '\uFFCA'..'\uFFCF'
  | '\uFFD2'..'\uFFD7'
  | '\uFFDA'..'\uFFDC'
  ;

//fragment
//UnicodeCombiningMark
//  :
//  '\u0300'..'\u034E'
//  | '\u0360'..'\u0362'
//  | '\u0483'..'\u0486'
//  | '\u0591'..'\u05A1'
//  | '\u05A3'..'\u05B9'
//  | '\u05BB'..'\u05BD'
//  | '\u05BF'
//  | '\u05C1'..'\u05C2'
//  | '\u05C4'
//  | '\u064B'..'\u0655'
//  | '\u0670'
//  | '\u06D6'..'\u06DC'
//  | '\u06DF'..'\u06E4'
//  | '\u06E7'..'\u06E8'
//  | '\u06EA'..'\u06ED'
//  | '\u0711'
//  | '\u0730'..'\u074A'
//  | '\u07A6'..'\u07B0'
//  | '\u0901'..'\u0903'
//  | '\u093C'
//  | '\u093E'..'\u094D'
//  | '\u0951'..'\u0954'
//  | '\u0962'..'\u0963'
//  | '\u0981'..'\u0983'
//  | '\u09BC'..'\u09C4'
//  | '\u09C7'..'\u09C8'
//  | '\u09CB'..'\u09CD'
//  | '\u09D7'
//  | '\u09E2'..'\u09E3'
//  | '\u0A02'
//  | '\u0A3C'
//  | '\u0A3E'..'\u0A42'
//  | '\u0A47'..'\u0A48'
//  | '\u0A4B'..'\u0A4D'
//  | '\u0A70'..'\u0A71'
//  | '\u0A81'..'\u0A83'
//  | '\u0ABC'
//  | '\u0ABE'..'\u0AC5'
//  | '\u0AC7'..'\u0AC9'
//  | '\u0ACB'..'\u0ACD'
//  | '\u0B01'..'\u0B03'
//  | '\u0B3C'
//  | '\u0B3E'..'\u0B43'
//  | '\u0B47'..'\u0B48'
//  | '\u0B4B'..'\u0B4D'
//  | '\u0B56'..'\u0B57'
//  | '\u0B82'..'\u0B83'
//  | '\u0BBE'..'\u0BC2'
//  | '\u0BC6'..'\u0BC8'
//  | '\u0BCA'..'\u0BCD'
//  | '\u0BD7'
//  | '\u0C01'..'\u0C03'
//  | '\u0C3E'..'\u0C44'
//  | '\u0C46'..'\u0C48'
//  | '\u0C4A'..'\u0C4D'
//  | '\u0C55'..'\u0C56'
//  | '\u0C82'..'\u0C83'
//  | '\u0CBE'..'\u0CC4'
//  | '\u0CC6'..'\u0CC8'
//  | '\u0CCA'..'\u0CCD'
//  | '\u0CD5'..'\u0CD6'
//  | '\u0D02'..'\u0D03'
//  | '\u0D3E'..'\u0D43'
//  | '\u0D46'..'\u0D48'
//  | '\u0D4A'..'\u0D4D'
//  | '\u0D57'
//  | '\u0D82'..'\u0D83'
//  | '\u0DCA'
//  | '\u0DCF'..'\u0DD4'
//  | '\u0DD6'
//  | '\u0DD8'..'\u0DDF'
//  | '\u0DF2'..'\u0DF3'
//  | '\u0E31'
//  | '\u0E34'..'\u0E3A'
//  | '\u0E47'..'\u0E4E'
//  | '\u0EB1'
//  | '\u0EB4'..'\u0EB9'
//  | '\u0EBB'..'\u0EBC'
//  | '\u0EC8'..'\u0ECD'
//  | '\u0F18'..'\u0F19'
//  | '\u0F35'
//  | '\u0F37'
//  | '\u0F39'
//  | '\u0F3E'..'\u0F3F'
//  | '\u0F71'..'\u0F84'
//  | '\u0F86'..'\u0F87'
//  | '\u0F90'..'\u0F97'
//  | '\u0F99'..'\u0FBC'
//  | '\u0FC6'
//  | '\u102C'..'\u1032'
//  | '\u1036'..'\u1039'
//  | '\u1056'..'\u1059'
//  | '\u17B4'..'\u17D3'
//  | '\u18A9'
//  | '\u20D0'..'\u20DC'
//  | '\u20E1'
//  | '\u302A'..'\u302F'
//  | '\u3099'..'\u309A'
//  | '\uFB1E'
//  | '\uFE20'..'\uFE23'
//  ;

fragment
UnicodeDigit
  :
  '\u0030'..'\u0039'
  | '\u0660'..'\u0669'
  | '\u06F0'..'\u06F9'
  | '\u0966'..'\u096F'
  | '\u09E6'..'\u09EF'
  | '\u0A66'..'\u0A6F'
  | '\u0AE6'..'\u0AEF'
  | '\u0B66'..'\u0B6F'
  | '\u0BE7'..'\u0BEF'
  | '\u0C66'..'\u0C6F'
  | '\u0CE6'..'\u0CEF'
  | '\u0D66'..'\u0D6F'
  | '\u0E50'..'\u0E59'
  | '\u0ED0'..'\u0ED9'
  | '\u0F20'..'\u0F29'
  | '\u1040'..'\u1049'
  | '\u1369'..'\u1371'
  | '\u17E0'..'\u17E9'
  | '\u1810'..'\u1819'
  | '\uFF10'..'\uFF19'
  ;

fragment
UnicodeConnectorPunctuation
  :
  '\u005F'
  | '\u203F'..'\u2040'
  | '\u30FB'
  | '\uFE33'..'\uFE34'
  | '\uFE4D'..'\uFE4F'
  | '\uFF3F'
  | '\uFF65'
  ;

Comment
  :
  '/*' (options {greedy=false;}: .)* '*/' 
                                          {
                                           $channel = HIDDEN;
                                          }
  ;

LineComment
  :
  '//' ~(LT )* 
               {
                $channel = HIDDEN;
               }
  ;

Include
  :
  '#include' ~(LT )* 
                     {
                      $channel = HIDDEN;
                     }
  ;

LT
  :
  '\n'
  | '\r'
  | '\u2028'
  | '\u2029'
  ;

WhiteSpace
  :
  (
    '\t'
    | '\v'
    | '\f'
    | ' '
    | '\u00A0'
  )
  
   {
    $channel = HIDDEN;
   }
  ;
