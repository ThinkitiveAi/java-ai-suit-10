import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Avatar,
  Typography,
  IconButton,
  TextField,
  InputAdornment,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  CircularProgress,
  TableSortLabel,
  Pagination,
  Stack,
  Skeleton,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import AddPatientModal from './AddPatientModal';
// import { ErrorBoundary } from './ErrorBoundary';
// import OfflineIndicator from './OfflineIndicator';

// Figma color codes (replace with actual codes from Figma)
const COLORS = {
  primary: '#0052CC', // Example: Primary CTA
  background: '#F4F5F7',
  tableHeader: '#E3E6EA',
  textPrimary: '#172B4D',
  textSecondary: '#6B778C',
  border: '#DFE1E6',
  action: '#42526E',
  error: '#FF5630',
};

export interface Patient {
  id: string;
  name: string;
  dateOfBirth: string;
  contactDetails: string;
  lastVisit: string;
}

const mockPatients: Patient[] = [
  { id: 'P001', name: 'John Doe', dateOfBirth: '1985-06-15', contactDetails: '555-1234', lastVisit: '2024-05-01' },
  { id: 'P002', name: 'Jane Smith', dateOfBirth: '1990-09-22', contactDetails: '555-5678', lastVisit: '2024-05-10' },
  { id: 'P003', name: 'Alice Johnson', dateOfBirth: '1978-12-03', contactDetails: '555-8765', lastVisit: '2024-04-28' },
  { id: 'P004', name: 'Bob Brown', dateOfBirth: '1982-03-19', contactDetails: '555-4321', lastVisit: '2024-05-12' },
  { id: 'P005', name: 'Charlie Green', dateOfBirth: '1995-11-30', contactDetails: '555-2468', lastVisit: '2024-05-05' },
  { id: 'P006', name: 'Diana Prince', dateOfBirth: '1988-07-07', contactDetails: '555-1357', lastVisit: '2024-05-08' },
  { id: 'P007', name: 'Ethan Hunt', dateOfBirth: '1975-01-25', contactDetails: '555-9753', lastVisit: '2024-04-30' },
  { id: 'P008', name: 'Fiona Gallagher', dateOfBirth: '1992-10-14', contactDetails: '555-8642', lastVisit: '2024-05-11' },
  { id: 'P009', name: 'George Martin', dateOfBirth: '1983-05-21', contactDetails: '555-7531', lastVisit: '2024-05-03' },
  { id: 'P010', name: 'Hannah Lee', dateOfBirth: '1998-02-17', contactDetails: '555-1597', lastVisit: '2024-05-09' },
  { id: 'P011', name: 'Ian Curtis', dateOfBirth: '1987-08-29', contactDetails: '555-9513', lastVisit: '2024-05-06' },
  { id: 'P012', name: 'Julia Roberts', dateOfBirth: '1979-04-11', contactDetails: '555-3571', lastVisit: '2024-05-02' },
  { id: 'P013', name: 'Kevin Spacey', dateOfBirth: '1981-12-19', contactDetails: '555-7539', lastVisit: '2024-05-07' },
  { id: 'P014', name: 'Laura Palmer', dateOfBirth: '1993-06-23', contactDetails: '555-2584', lastVisit: '2024-05-04' },
  { id: 'P015', name: 'Mike Ross', dateOfBirth: '1986-10-05', contactDetails: '555-6547', lastVisit: '2024-05-13' },
  { id: 'P016', name: 'Nina Simone', dateOfBirth: '1991-03-12', contactDetails: '555-8524', lastVisit: '2024-05-14' },
];

const PAGE_SIZE = 10;

type SortKey = 'dateOfBirth' | 'lastVisit';
type SortOrder = 'asc' | 'desc';

const PatientListView: React.FC = () => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState<SortKey>('dateOfBirth');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');
  const [filter, setFilter] = useState('');
  const [isAddModalOpen, setAddModalOpen] = useState(false);

  // Debounce search input
  useEffect(() => {
    const handler = setTimeout(() => {
      setSearch(searchInput);
      setPage(1);
    }, 300);
    return () => clearTimeout(handler);
  }, [searchInput]);

  // Simulate loading and error
  useEffect(() => {
    setLoading(true);
    setError(null);
    const timer = setTimeout(() => {
      // Simulate error: setError('Failed to load patients'); setLoading(false);
      setPatients(mockPatients);
      setLoading(false);
    }, 800);
    return () => clearTimeout(timer);
  }, []);

  // Filter, search, sort, and paginate patients
  const filteredPatients = useMemo(() => {
    let data = [...patients];
    if (search) {
      data = data.filter((p) =>
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.id.toLowerCase().includes(search.toLowerCase())
      );
    }
    if (filter) {
      // Example: filter by last visit month/year, etc. (not implemented, placeholder)
    }
    data.sort((a, b) => {
      const aVal = a[sortKey];
      const bVal = b[sortKey];
      if (sortOrder === 'asc') return aVal.localeCompare(bVal);
      return bVal.localeCompare(aVal);
    });
    return data;
  }, [patients, search, filter, sortKey, sortOrder]);

  const paginatedPatients = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return filteredPatients.slice(start, start + PAGE_SIZE);
  }, [filteredPatients, page]);

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortOrder((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortOrder('asc');
    }
  };

  // Add handler to add new patient to the top of the list
  const handleAddPatient = (newPatient: any) => {
    setPatients((prev) => [newPatient, ...prev]);
  };

  const renderSkeletonRows = () => (
    Array.from({ length: 10 }).map((_, idx) => (
      <TableRow key={idx}>
        {Array.from({ length: 6 }).map((__, colIdx) => (
          <TableCell key={colIdx}>
            <Skeleton variant="rectangular" width="100%" height={32} />
          </TableCell>
        ))}
      </TableRow>
    ))
  );

  // Responsive full width, no max-width or centering
  return (
    <Box sx={{ width: '100vw', minHeight: '100vh', background: COLORS.background, p: { xs: 1, md: 3 }, m: 0 }}>
      <Paper elevation={0} sx={{ width: '100%', p: { xs: 1, md: 3 }, boxShadow: 'none', background: COLORS.background }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={2}>
          <Typography variant="h5" sx={{ color: COLORS.textPrimary, fontWeight: 600 }}>
            Patient List
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            sx={{ background: COLORS.primary, color: '#fff', borderRadius: 2, textTransform: 'none', fontWeight: 600, '&:hover': { background: '#003580' } }}
            onClick={() => setAddModalOpen(true)}
          >
            Add Patient
          </Button>
        </Stack>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={2} alignItems={{ sm: 'center' }}>
          <TextField
            placeholder="Search by name or ID"
            size="small"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: COLORS.textSecondary }} />
                </InputAdornment>
              ),
            }}
            sx={{ width: { xs: '100%', sm: 300 } }}
          />
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Filter</InputLabel>
            <Select
              value={filter}
              label="Filter"
              onChange={(e) => setFilter(e.target.value)}
            >
              <MenuItem value="">None</MenuItem>
              {/* Placeholder for future filter options */}
            </Select>
          </FormControl>
        </Stack>
        <TableContainer component={Paper} sx={{ boxShadow: 'none', borderRadius: 2, border: `1px solid ${COLORS.border}` }}>
          <Table sx={{ minWidth: 650 }} aria-label="patient table">
            <TableHead>
              <TableRow sx={{ background: COLORS.tableHeader }}>
                <TableCell sx={{ fontWeight: 600, color: COLORS.textPrimary }}>Patient Id</TableCell>
                <TableCell sx={{ fontWeight: 600, color: COLORS.textPrimary }}>Avatar & Name</TableCell>
                <TableCell sx={{ fontWeight: 600, color: COLORS.textPrimary }}>
                  <TableSortLabel
                    active={sortKey === 'dateOfBirth'}
                    direction={sortKey === 'dateOfBirth' ? sortOrder : 'asc'}
                    onClick={() => handleSort('dateOfBirth')}
                  >
                    Date of Birth
                  </TableSortLabel>
                </TableCell>
                <TableCell sx={{ fontWeight: 600, color: COLORS.textPrimary }}>Contact Details</TableCell>
                <TableCell sx={{ fontWeight: 600, color: COLORS.textPrimary }}>
                  <TableSortLabel
                    active={sortKey === 'lastVisit'}
                    direction={sortKey === 'lastVisit' ? sortOrder : 'asc'}
                    onClick={() => handleSort('lastVisit')}
                  >
                    Last Visit
                  </TableSortLabel>
                </TableCell>
                <TableCell sx={{ fontWeight: 600, color: COLORS.textPrimary }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading
                ? renderSkeletonRows()
                : error
                  ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ color: COLORS.error }}>
                        {error}
                      </TableCell>
                    </TableRow>
                  )
                  : paginatedPatients.length === 0
                    ? (
                      <TableRow>
                        <TableCell colSpan={6} align="center">
                          No patients found.
                        </TableCell>
                      </TableRow>
                    )
                    : paginatedPatients.map((patient) => (
                        <TableRow key={patient.id} hover>
                          <TableCell sx={{ color: COLORS.textPrimary }}>{patient.id}</TableCell>
                          <TableCell>
                            <Stack direction="row" alignItems="center" spacing={2}>
                              <Avatar sx={{ bgcolor: COLORS.primary }}>
                                {patient.name.split(' ').map((n) => n[0]).join('')}
                              </Avatar>
                              <Typography sx={{ color: COLORS.textPrimary, fontWeight: 500 }}>{patient.name}</Typography>
                            </Stack>
                          </TableCell>
                          <TableCell sx={{ color: COLORS.textSecondary }}>{patient.dateOfBirth}</TableCell>
                          <TableCell sx={{ color: COLORS.textSecondary }}>{patient.contactDetails}</TableCell>
                          <TableCell sx={{ color: COLORS.textSecondary }}>{patient.lastVisit}</TableCell>
                          <TableCell>
                            <IconButton aria-label="view" sx={{ color: COLORS.action }}>
                              <VisibilityIcon />
                            </IconButton>
                            <IconButton aria-label="edit" sx={{ color: COLORS.primary }}>
                              <EditIcon />
                            </IconButton>
                            <IconButton aria-label="delete" sx={{ color: COLORS.error }}>
                              <DeleteIcon />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))
              }
            </TableBody>
          </Table>
        </TableContainer>
        <Stack direction="row" justifyContent="flex-end" alignItems="center" mt={2}>
          <Pagination
            count={Math.ceil(filteredPatients.length / PAGE_SIZE)}
            page={page}
            onChange={(_, value) => setPage(value)}
            color="primary"
            sx={{ '& .MuiPaginationItem-root': { color: COLORS.primary } }}
          />
        </Stack>
        <AddPatientModal
          open={isAddModalOpen}
          onClose={() => setAddModalOpen(false)}
          onAdd={handleAddPatient}
        />
      </Paper>
      {/* <OfflineIndicator /> */}
    </Box>
  );
};

export default PatientListView; 