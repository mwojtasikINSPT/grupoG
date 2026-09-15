param(
    [string]$SourceRoot = (Join-Path $PSScriptRoot '..\src'),
    [string]$OutputFile = (Join-Path $PSScriptRoot 'sistema-policia-completo.uxf')
)

$ErrorActionPreference = 'Stop'

function Escape-Xml([string]$value) {
    return [System.Security.SecurityElement]::Escape($value)
}

function Clean-Type([string]$type) {
    $clean = $type.Trim() -replace '\s+', ' '
    $clean = $clean -replace 'java\.lang\.', '' -replace 'java\.util\.', '' -replace 'java\.time\.', ''
    $clean = $clean -replace '(models|dtos|daos|controllers|views|exceptions)\.', ''
    return $clean
}

function Visibility-Symbol([string]$visibility) {
    switch ($visibility) {
        'public' { return '+' }
        'protected' { return '#' }
        default { return '-' }
    }
}

function Get-JavaInfo([System.IO.FileInfo]$file) {
    $raw = Get-Content -Raw -LiteralPath $file.FullName -Encoding UTF8
    $withoutBlocks = [regex]::Replace($raw, '/\*.*?\*/', '', 'Singleline')
    $code = [regex]::Replace($withoutBlocks, '//.*$', '', 'Multiline')

    $package = [regex]::Match($code, '(?m)^\s*package\s+([\w.]+)\s*;').Groups[1].Value
    $declaration = [regex]::Match($code, '(?m)^\s*public\s+(?:(abstract)\s+)?(class|interface|enum)\s+(\w+)([^\{]*)\{')
    if (-not $declaration.Success) { return $null }

    $name = $declaration.Groups[3].Value
    $kind = $declaration.Groups[2].Value
    if ($declaration.Groups[1].Success) { $kind = 'abstract class' }
    $tail = $declaration.Groups[4].Value.Trim()

    $attributes = [System.Collections.Generic.List[string]]::new()
    $methods = [System.Collections.Generic.List[string]]::new()

    foreach ($line in ($code -split "`r?`n")) {
        $field = [regex]::Match($line, '^\s*(public|private|protected)\s+(?:(static)\s+)?(?:(final)\s+)?([\w<>,.?\[\] ]+)\s+(\w+)\s*(?:=[^;]*)?;\s*$')
        if ($field.Success) {
            $fieldName = $field.Groups[5].Value
            if ($fieldName -ne 'serialVersionUID') {
                $symbol = Visibility-Symbol $field.Groups[1].Value
                $modifier = if ($field.Groups[2].Success) { '{static} ' } else { '' }
                $attributes.Add("$symbol$modifier$fieldName`: $(Clean-Type $field.Groups[4].Value)")
            }
            continue
        }

        $method = [regex]::Match($line, '^\s*(public|private|protected)\s+(?:(static)\s+)?(?:(abstract)\s+)?([\w<>,.?\[\] ]+)\s+(\w+)\s*\(([^)]*)\)')
        if ($method.Success) {
            $methodName = $method.Groups[5].Value
            if ($methodName -match '^(get|set|is)[A-Z]' -or $methodName -eq 'toString') { continue }
            $symbol = Visibility-Symbol $method.Groups[1].Value
            $modifier = if ($method.Groups[2].Success) { '{static} ' } else { '' }
            $parameters = (Clean-Type $method.Groups[6].Value)
            $returnType = Clean-Type $method.Groups[4].Value
            $methods.Add("$symbol$modifier$methodName($parameters): $returnType")
        }
    }

    if ($kind -eq 'enum' -and $name -eq 'Rol') {
        $attributes.Clear()
        @('ADMINISTRADOR', 'INVESTIGADOR', 'VIGILANTE') | ForEach-Object { $attributes.Add($_) }
    }

    # En una interfaz los métodos son públicos aunque Java permita omitir "public".
    if ($name -eq 'IGenericDAO') {
        $methods.Clear()
        $methods.Add('+guardar(entidad: T): void')
        $methods.Add('+obtenerTodos(): List<T>')
        $methods.Add('+buscarPorId(id: String): T')
        $methods.Add('+actualizar(entidad: T): void')
        $methods.Add('+eliminar(id: String): void')
    }

    # Se muestran los constructores de las excepciones porque explican
    # qué información contextual recibe cada error personalizado.
    if ($package -eq 'exceptions') {
        if ($name -eq 'ObjetoNoEncontradoException') {
            $methods.Add("+$name(tipoObjeto: String, id: String)")
        } else {
            $methods.Add("+$name(tipoObjeto: String, motivo: String)")
        }
    }

    [pscustomobject]@{
        Name = $name
        Package = $package
        Kind = $kind
        Tail = $tail
        Attributes = @($attributes)
        Methods = @($methods)
        File = $file.FullName
    }
}

$infos = Get-ChildItem -LiteralPath $SourceRoot -Recurse -Filter '*.java' |
    ForEach-Object { Get-JavaInfo $_ } |
    Where-Object { $_ -ne $null }

$positions = @{
    # VIEWS: extremo izquierdo. Las vistas quedan alineadas con sus controladores.
    'Main'                 = @(100, 140, 350, 150)
    'UIHelper'             = @(520, 140, 620, 280)
    'MenuAdministrador'    = @(100, 500, 350, 150)
    'MenuInvestigador'     = @(500, 500, 350, 150)
    'MenuVigilante'        = @(900, 500, 350, 150)
    'GestionUsuarios'      = @(100, 900, 430, 260)
    'GestionOperativa'     = @(600, 900, 430, 300)
    'GestionJudicial'      = @(100, 1280, 430, 300)
    'ConsultaVigilante'    = @(600, 1280, 430, 260)

    # CONTROLLERS: segunda columna, entre vistas y persistencia.
    'MenuController'       = @(1600, 140, 480, 150)
    'LoginController'      = @(2200, 140, 480, 200)
    'UsuariosController'   = @(1600, 600, 480, 250)
    'BancarioController'   = @(2200, 600, 520, 350)
    'AsaltosController'    = @(1600, 1150, 520, 320)
    'JudicialController'   = @(2200, 1150, 520, 300)

    # DAOS: tercera columna. Los pares se alinean con las entidades que persisten.
    'IGenericDAO'          = @(3400, 140, 700, 260)
    'UsuarioDAO'           = @(3000, 520, 650, 230)
    'VigilanteDAO'         = @(3800, 520, 650, 230)
    'EntidadBancariaDAO'   = @(3000, 840, 650, 230)
    'SucursalDAO'          = @(3800, 840, 650, 230)
    'ContratoVigilanciaDAO'= @(3000, 1160, 650, 250)
    'BandaDAO'             = @(3800, 1160, 650, 230)
    'AsaltanteDAO'         = @(3000, 1500, 650, 230)
    'AsaltoDAO'            = @(3800, 1500, 650, 230)
    'JuezDAO'              = @(3000, 1840, 650, 230)
    'CasoJudicialDAO'      = @(3800, 1840, 650, 230)

    # MODELS: extremo derecho, con el dominio agrupado por tema.
    'Usuario'              = @(5700, 140, 560, 220)
    'Rol'                  = @(6700, 140, 430, 180)
    'UsuarioAdministrador' = @(5000, 500, 480, 180)
    'UsuarioInvestigador'  = @(5600, 500, 480, 180)
    'UsuarioVigilante'     = @(6200, 500, 500, 220)
    'Vigilante'            = @(5000, 940, 480, 190)
    'EntidadBancaria'      = @(6600, 940, 500, 190)
    'ContratoVigilancia'   = @(5550, 1300, 560, 240)
    'Sucursal'             = @(6500, 1300, 560, 240)
    'Banda'                = @(5000, 1780, 480, 190)
    'Asaltante'            = @(5550, 1780, 560, 260)
    'Asalto'               = @(6200, 1780, 560, 240)
    'Juez'                 = @(6850, 1780, 480, 210)
    'CasoJudicial'         = @(6400, 2220, 600, 250)

    # DTO: debajo de las vistas, separados del modelo del dominio.
    'UsuarioLoginDTO'      = @(100, 2050, 500, 180)
    'VigilanteAsignadoDTO' = @(100, 2400, 520, 210)
    'VigilanteContratoDTO' = @(700, 2400, 560, 260)
    'DetalleDelitoDTO'     = @(100, 2820, 540, 240)
    'HistorialAsaltanteDTO'= @(700, 2820, 560, 250)
    'ReporteSucursalDTO'   = @(100, 3220, 650, 300)

    # EXCEPTIONS: debajo de los DAO, cerca de IGenericDAO.
    'ErrorAlGuardarException'       = @(3000, 2480, 650, 180)
    'ErrorAlLeerException'          = @(3800, 2480, 650, 180)
    'ErrorAlActualizarException'    = @(3000, 2780, 650, 180)
    'ErrorAlEliminarException'      = @(3800, 2780, 650, 180)
    'ObjetoNoEncontradoException'   = @(3400, 3080, 650, 180)
}

$nodes = @{}
foreach ($info in $infos) {
    if (-not $positions.ContainsKey($info.Name)) {
        throw "Falta definir la ubicación UML de $($info.Name)."
    }
    $p = $positions[$info.Name]
    $lineCount = 5 + $info.Attributes.Count + $info.Methods.Count
    $minimumHeight = 42 + ($lineCount * 16)
    $nodes[$info.Name] = [pscustomobject]@{
        Info = $info
        X = $p[0]
        Y = $p[1]
        W = $p[2]
        H = [math]::Max($p[3], $minimumHeight)
    }
}

function New-ClassElement($node) {
    $info = $node.Info
    $stereotype = switch ($info.Kind) {
        'interface' { '<<interface>>' }
        'enum' { '<<enumeration>>' }
        'abstract class' { '<<abstract>>' }
        default { "<<$($info.Package)>>" }
    }
    $title = if ($info.Kind -eq 'abstract class') { "*$($info.Name)*" } else { $info.Name }
    $body = @($stereotype, $title, '--') + $info.Attributes + @('--') + $info.Methods
    $panel = Escape-Xml ($body -join "`n")
    return @"
  <element>
    <id>UMLClass</id>
    <coordinates><x>$($node.X)</x><y>$($node.Y)</y><w>$($node.W)</w><h>$($node.H)</h></coordinates>
    <panel_attributes>$panel</panel_attributes>
    <additional_attributes></additional_attributes>
  </element>
"@
}

function New-PackageElement([string]$name, [int]$x, [int]$y, [int]$w, [int]$h) {
    $panel = Escape-Xml $name
    return @"
  <element>
    <id>UMLPackage</id>
    <coordinates><x>$x</x><y>$y</y><w>$w</w><h>$h</h></coordinates>
    <panel_attributes>$panel</panel_attributes>
    <additional_attributes></additional_attributes>
  </element>
"@
}

function New-NoteElement([string]$text, [int]$x, [int]$y, [int]$w, [int]$h) {
    $panel = Escape-Xml $text
    return @"
  <element>
    <id>UMLNote</id>
    <coordinates><x>$x</x><y>$y</y><w>$w</w><h>$h</h></coordinates>
    <panel_attributes>$panel</panel_attributes>
    <additional_attributes></additional_attributes>
  </element>
"@
}

function Get-ConnectionPoints($first, $second) {
    $c1x = $first.X + ($first.W / 2); $c1y = $first.Y + ($first.H / 2)
    $c2x = $second.X + ($second.W / 2); $c2y = $second.Y + ($second.H / 2)
    $dx = $c2x - $c1x; $dy = $c2y - $c1y
    if ([math]::Abs($dx) -gt [math]::Abs($dy)) {
        $x1 = if ($dx -gt 0) { $first.X + $first.W } else { $first.X }
        $y1 = $c1y
        $x2 = if ($dx -gt 0) { $second.X } else { $second.X + $second.W }
        $y2 = $c2y
    } else {
        $x1 = $c1x
        $y1 = if ($dy -gt 0) { $first.Y + $first.H } else { $first.Y }
        $x2 = $c2x
        $y2 = if ($dy -gt 0) { $second.Y } else { $second.Y + $second.H }
    }
    return @([int]$x1, [int]$y1, [int]$x2, [int]$y2)
}

function New-RelationElement([string]$from, [string]$to, [string]$kind, [string]$label = '', [string]$mFrom = '', [string]$mTo = '') {
    if (-not $nodes.ContainsKey($from) -or -not $nodes.ContainsKey($to)) { return '' }

    # UMLet coloca la punta declarada por "lt=<" en el primer extremo.
    # Por eso herencia/realización/dependencia conectan primero el elemento destino.
    if ($kind -in @('generalization', 'realization', 'dependency')) {
        $first = $nodes[$to]; $second = $nodes[$from]
    } else {
        $first = $nodes[$from]; $second = $nodes[$to]
    }
    if ($kind -eq 'dependency' -and $from -eq 'UIHelper' -and $to -eq 'Rol') {
        # Dependencia larga por el borde superior para no cortar las capas centrales.
        $points = @(
            [int]($first.X + ($first.W / 2)), [int]$first.Y,
            [int]($first.X + ($first.W / 2)), 20,
            [int]($second.X + ($second.W / 2)), 20,
            [int]($second.X + ($second.W / 2)), [int]$second.Y
        )
    } elseif ($kind -eq 'dependency' -and $from -eq 'LoginController' -and $to -eq 'UsuarioLoginDTO') {
        # El DTO se conecta por el pasillo entre paquetes, sin cruzar las vistas.
        $points = @(
            [int]($first.X + $first.W), [int]($first.Y + ($first.H / 2)),
            2800, [int]($first.Y + ($first.H / 2)),
            2800, [int]($second.Y + ($second.H / 2)),
            [int]($second.X + $second.W), [int]($second.Y + ($second.H / 2))
        )
    } elseif ($kind -eq 'dependency' -and $from -eq 'IGenericDAO' -and $to -like '*Exception') {
        # Las excepciones se conectan por el corredor central del paquete DAO.
        $corridorX = 3725
        if ($first.X -lt $corridorX) {
            $targetX = $first.X + $first.W
            $targetY = $first.Y + ($first.H / 2)
        } elseif ($first.X -gt $corridorX) {
            $targetX = $first.X
            $targetY = $first.Y + ($first.H / 2)
        } else {
            $targetX = $first.X + ($first.W / 2)
            $targetY = $first.Y
        }
        $points = @(
            [int]$targetX, [int]$targetY,
            $corridorX, [int]$targetY,
            $corridorX, [int]($second.Y + $second.H),
            [int]($second.X + ($second.W / 2)), [int]($second.Y + $second.H)
        )
    } elseif ($kind -eq 'dependency' -and $from -eq 'Main' -and $to -like 'Menu*') {
        # Navegación por el espacio libre entre Main/UIHelper y los menús.
        $points = @(
            [int]($first.X + ($first.W / 2)), [int]$first.Y,
            [int]($first.X + ($first.W / 2)), 450,
            [int]($second.X + ($second.W / 2)), 450,
            [int]($second.X + ($second.W / 2)), [int]($second.Y + $second.H)
        )
    } elseif ($kind -eq 'realization' -and $to -eq 'IGenericDAO') {
        # Las realizaciones bajan por corredores laterales del paquete DAO.
        # Así no atraviesan los cuadros de otros DAO ubicados en filas anteriores.
        $rowIndex = [math]::Max(0, [math]::Round(($second.Y - 520) / 320))
        if ($second.X -lt 3600) {
            $corridorX = 2890 + (10 * $rowIndex)
            $startX = $first.X
            $startY = $first.Y + 60 + (20 * $rowIndex)
            $endX = $second.X
            $endY = $second.Y + ($second.H / 2)
        } else {
            $corridorX = 4510 - (10 * $rowIndex)
            $startX = $first.X + $first.W
            $startY = $first.Y + 60 + (20 * $rowIndex)
            $endX = $second.X + $second.W
            $endY = $second.Y + ($second.H / 2)
        }
        $points = @([int]$startX, [int]$startY, [int]$corridorX, [int]$startY, [int]$corridorX, [int]$endY, [int]$endX, [int]$endY)
    } else {
        $points = @(Get-ConnectionPoints $first $second)
    }

    $xs = for ($i = 0; $i -lt $points.Count; $i += 2) { $points[$i] }
    $ys = for ($i = 1; $i -lt $points.Count; $i += 2) { $points[$i] }
    $minX = ($xs | Measure-Object -Minimum).Minimum - 20
    $minY = ($ys | Measure-Object -Minimum).Minimum - 20
    $maxX = ($xs | Measure-Object -Maximum).Maximum + 20
    $maxY = ($ys | Measure-Object -Maximum).Maximum + 20
    $width = [math]::Max(40, $maxX - $minX)
    $height = [math]::Max(40, $maxY - $minY)
    $relativePoints = for ($i = 0; $i -lt $points.Count; $i += 2) {
        "$(($points[$i] - $minX));$(($points[$i + 1] - $minY))"
    }
    $relative = $relativePoints -join ';'

    $lineType = switch ($kind) {
        'generalization' { 'lt=<<-' }
        'realization' { 'lt=<<.' }
        'dependency' { 'lt=<.' }
        'aggregation' { 'lt=<>-' }
        'composition' { 'lt=<<>-' }
        default { 'lt=-' }
    }
    $attributes = @($lineType)
    if ($label) { $attributes += $label }
    if ($mFrom) { $attributes += "m2=$mFrom" }
    if ($mTo) { $attributes += "m1=$mTo" }
    $panel = Escape-Xml ($attributes -join "`n")
    return @"
  <element>
    <id>Relation</id>
    <coordinates><x>$minX</x><y>$minY</y><w>$width</w><h>$height</h></coordinates>
    <panel_attributes>$panel</panel_attributes>
    <additional_attributes>$relative</additional_attributes>
  </element>
"@
}

$relations = @(
    # Herencia de usuarios.
    @('UsuarioAdministrador','Usuario','generalization','','',''),
    @('UsuarioInvestigador','Usuario','generalization','','',''),
    @('UsuarioVigilante','Usuario','generalization','','',''),

    # Asociaciones del dominio. Las clases intermedias resuelven relaciones N:M.
    @('UsuarioVigilante','Vigilante','association','identidad operativa','1','1'),
    @('Sucursal','EntidadBancaria','association','pertenece a','0..*','1'),
    @('Asaltante','Banda','association','integra','0..*','0..1'),
    @('Asalto','Asaltante','association','autor','0..*','1'),
    @('Asalto','Sucursal','association','ocurre en','0..*','1'),
    @('ContratoVigilancia','Sucursal','association','destino','0..*','1'),
    @('ContratoVigilancia','Vigilante','association','asignado','0..*','1'),
    @('CasoJudicial','Asalto','association','juzga hecho','0..1','1'),
    @('CasoJudicial','Juez','association','a cargo de','0..*','1'),

    # DTO compuestos para consultas e informes.
    @('HistorialAsaltanteDTO','DetalleDelitoDTO','aggregation','delitos','1','0..*'),
    @('ReporteSucursalDTO','VigilanteAsignadoDTO','aggregation','vigilantes','1','0..*'),

    # Todos los DAO realizan el contrato CRUD genérico.
    @('AsaltanteDAO','IGenericDAO','realization','','',''),
    @('AsaltoDAO','IGenericDAO','realization','','',''),
    @('BandaDAO','IGenericDAO','realization','','',''),
    @('CasoJudicialDAO','IGenericDAO','realization','','',''),
    @('ContratoVigilanciaDAO','IGenericDAO','realization','','',''),
    @('EntidadBancariaDAO','IGenericDAO','realization','','',''),
    @('JuezDAO','IGenericDAO','realization','','',''),
    @('SucursalDAO','IGenericDAO','realization','','',''),
    @('UsuarioDAO','IGenericDAO','realization','','',''),
    @('VigilanteDAO','IGenericDAO','realization','','',''),

    # Cada DAO persiste su entidad.
    @('AsaltanteDAO','Asaltante','dependency','persiste','',''),
    @('AsaltoDAO','Asalto','dependency','persiste','',''),
    @('BandaDAO','Banda','dependency','persiste','',''),
    @('CasoJudicialDAO','CasoJudicial','dependency','persiste','',''),
    @('ContratoVigilanciaDAO','ContratoVigilancia','dependency','persiste','',''),
    @('EntidadBancariaDAO','EntidadBancaria','dependency','persiste','',''),
    @('JuezDAO','Juez','dependency','persiste','',''),
    @('SucursalDAO','Sucursal','dependency','persiste','',''),
    @('UsuarioDAO','Usuario','dependency','persiste','',''),
    @('VigilanteDAO','Vigilante','dependency','persiste','',''),

    # El contrato DAO declara las excepciones de persistencia.
    @('IGenericDAO','ErrorAlGuardarException','dependency','guardar','',''),
    @('IGenericDAO','ErrorAlLeerException','dependency','leer','',''),
    @('IGenericDAO','ErrorAlActualizarException','dependency','actualizar','',''),
    @('IGenericDAO','ErrorAlEliminarException','dependency','eliminar','',''),
    @('IGenericDAO','ObjetoNoEncontradoException','dependency','buscar','',''),

    # Dependencias estables de los controladores (atributos DAO).
    @('AsaltosController','BandaDAO','association','','1','1'),
    @('AsaltosController','AsaltanteDAO','association','','1','1'),
    @('AsaltosController','AsaltoDAO','association','','1','1'),
    @('AsaltosController','SucursalDAO','association','','1','1'),
    @('BancarioController','SucursalDAO','association','','1','1'),
    @('BancarioController','VigilanteDAO','association','','1','1'),
    @('BancarioController','ContratoVigilanciaDAO','association','','1','1'),
    @('JudicialController','JuezDAO','association','','1','1'),
    @('JudicialController','CasoJudicialDAO','association','','1','1'),
    @('JudicialController','AsaltoDAO','association','','1','1'),
    @('LoginController','UsuarioDAO','association','','1','1'),
    @('LoginController','MenuController','association','','1','1'),
    @('UsuariosController','UsuarioDAO','association','','1','1'),
    @('UsuariosController','VigilanteDAO','association','','1','1'),
    @('LoginController','UsuarioLoginDTO','dependency','autentica','',''),

    # Vistas y navegación.
    @('Main','LoginController','dependency','inicia','',''),
    @('Main','UsuarioLoginDTO','dependency','crea','',''),
    @('Main','MenuAdministrador','dependency','abre','',''),
    @('Main','MenuInvestigador','dependency','abre','',''),
    @('Main','MenuVigilante','dependency','abre','',''),
    @('MenuAdministrador','GestionUsuarios','dependency','abre','',''),
    @('MenuAdministrador','GestionOperativa','dependency','abre','',''),
    @('MenuAdministrador','GestionJudicial','dependency','abre','',''),
    @('MenuInvestigador','GestionUsuarios','dependency','abre','',''),
    @('MenuInvestigador','GestionOperativa','dependency','abre','',''),
    @('MenuInvestigador','GestionJudicial','dependency','abre','',''),
    @('MenuVigilante','ConsultaVigilante','dependency','abre','',''),
    @('ConsultaVigilante','BancarioController','association','','1','1'),
    @('GestionJudicial','JudicialController','association','','1','1'),
    @('GestionJudicial','AsaltosController','association','','1','1'),
    @('GestionOperativa','UsuariosController','association','','1','1'),
    @('GestionOperativa','BancarioController','association','','1','1'),
    @('GestionUsuarios','UsuariosController','association','','1','1'),
    @('UIHelper','Rol','dependency','valida','',''),

    # Main representa la dependencia de entrada/salida con la utilidad compartida.
    # Las demás vistas también la utilizan, pero repetir ocho flechas haría ilegible
    # el paquete; sus llamadas ya quedan documentadas por los métodos de UIHelper.
    @('Main','UIHelper','dependency','','','')
)

$xmlParts = [System.Collections.Generic.List[string]]::new()
$xmlParts.Add('<?xml version="1.0" encoding="UTF-8"?>')
$xmlParts.Add('<diagram program="umletino" version="15.1">')
$xmlParts.Add('  <zoom_level>6</zoom_level>')

# Los paquetes se agregan primero para que funcionen como fondo del diagrama.
$xmlParts.Add((New-PackageElement 'VIEWS' 40 60 1260 1600))
$xmlParts.Add((New-PackageElement 'CONTROLLERS' 1450 60 1320 1500))
$xmlParts.Add((New-PackageElement 'DAOS' 2850 60 1700 2150))
$xmlParts.Add((New-PackageElement 'MODELS' 4850 60 2600 2550))
$xmlParts.Add((New-PackageElement 'DTOS' 40 1950 1280 1650))
$xmlParts.Add((New-PackageElement 'EXCEPTIONS' 2850 2350 1700 1000))
$xmlParts.Add((New-NoteElement "LEYENDA`nLínea + triángulo: herencia`nDiscontinua + triángulo: implementación`nLínea continua: asociación`nDiscontinua + flecha: dependencia`nRombo vacío: agregación" 4900 2800 1050 300))
$xmlParts.Add((New-NoteElement "UIHelper es una utilidad estática compartida por las vistas.`nSe muestra una sola dependencia desde Main para evitar ocho flechas repetidas." 6100 2800 1200 220))

# Las relaciones se dibujan antes que las clases para que las líneas no tapen los cuadros.
foreach ($relation in $relations) {
    $xmlParts.Add((New-RelationElement @relation))
}
foreach ($node in ($nodes.Values | Sort-Object Y, X)) {
    $xmlParts.Add((New-ClassElement $node))
}
$xmlParts.Add('</diagram>')

$outputDirectory = Split-Path -Parent $OutputFile
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
[System.IO.File]::WriteAllText($OutputFile, ($xmlParts -join "`n"), [System.Text.UTF8Encoding]::new($false))

# Verifica que el resultado sea XML válido antes de entregarlo.
[xml](Get-Content -Raw -LiteralPath $OutputFile -Encoding UTF8) | Out-Null
Write-Output $OutputFile
